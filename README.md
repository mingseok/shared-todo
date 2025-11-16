# 공유 ToDo 리스트 서비스

## 🚀 프로젝트 목표

공유 가능한 ToDo 리스트 서비스를 구현하면서 단순한 CRUD를 넘어서, 실제 서비스에서 발생할 수 있는 

예외 상황과 문제들을 먼저 찾아보고, 해결할 수 있는 구조를 운영 환경까지 고려해 설계했습니다.


- 전체 설계는 유지보수성과 협업을 우선순위로 두었습니다.

- 책임을 분리하고, 확장 가능한 구조를 만들며, 다른 개발자가 읽었을 때 이해하기 쉬운 흐름을 목표로 했습니다.

<br/>

### 처음 설계 단계에서 2가지를 중점적으로 고민했습니다.

문제 발견 관점으로 요구사항 해석
- “사용자가 공유받은 ToDo는 조회만 가능하다 → 수정/삭제 요청이 들어오면 어떻게 검증해야 하나?“

- “ToDo 순서 변경이 잦다면, 레이스 컨디션이 발생할 가능성은 없나?”
- “관리자 권한 정책이 아직 미정인데, 어떻게 구조적으로 유연하게 열어둘까?”
- “삭제한 ToDo를 나중에 복구한다면, 소프트 삭제가 더 적합하지 않을까?”

<br/>

협업을 위한 구조와 트레이드오프 고려
- 권한 정책 확정 전이기 때문에, 관리자를 위한 확장 포인트를 남겨두는 방식으로 설계

- 순서 변경을 구현하는 여러 방식 중, 단순 구현 vs 정렬 안정성 vs 동시성 안전성 사이의 트레이드오프 검토
- 삭제 기능은 향후 “되돌리기(Undo)” 기능 고려를 위해 소프트 삭제 구조 선택

<br/><br/>

## CRUD 기능 구현 과정과 설계 방향

CRUD 기능은 단순히 데이터를 생성, 조회, 수정, 삭제하는 수준이 아니라, 

`권한 처리`, `조회 성능`, `예외 흐름`, 향후 기능 확장 가능성 등을 고려해 설계했습니다.


<br/>


### 저장(Create)

ToDo 생성 시 필드 구성을 최소화해 이후 태그나 순서 기능이 추가되더라도 확장할 수 있도록 했습니다.

D-Day는 매번 달라지는 값이기 때문에 DB에 저장하지 않고 조회 시 계산하는 방식으로 결정했습니다.

- D-Day 저장: 하지 않음

- D-Day 계산: 조회 시점에서 Response DTO에서 계산
- 관리자 정책: 미정이므로 현재는 작성자 기준으로만 생성 가능

```java
public Todo create(TodoCreateRequest request, Long memberId) {
    Todo todo = Todo.create(request.getTitle(), request.getDueDate(), memberId);
    return todoRepository.save(todo);
}
```

<br/>

### 조회(Read)

단건 조회와 목록 조회 모두 실제 서비스에서 자주 호출되는 기능이기 때문에 예외 처리와 성능 개선을 우선적으로 고려했습니다.

**단건 조회**
- `Dirty Checking` 비활성화하기 위해 `@Transactional(readOnly = true)` 사용

- 존재하지 않는 ID 조회 시 명확한 예외 반환

```java
@Transactional(readOnly = true)
public TodoDetailResponse findDetail(Long todoId) {
    Todo todo = todoRepository.findById(todoId)
            .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));
    return TodoDetailResponse.from(todo);
}
```

<br/>

**목록 조회**

목록 조회는 아직 리팩토링 전 단계로, 조건 조합별로 별도의 쿼리 메서드를 작성해 처리하는 방식으로 구현해두었습니다.

삭제된 항목을 제외하기 위해 모든 조회 메서드에는 `DeletedAtIsNull` 조건이 포함되어 있으며,

사용자(memberId), 완료 여부(completed), 특정 ID 목록(idIn) 등 다양한 조합을 지원하기 위해 메서드 수가 많아진 상태입니다.

```java
@Transactional(readOnly = true)
public interface TodoRepository extends JpaRepository<Todo, Long> {

    Optional<Todo> findByIdAndDeletedAtIsNull(Long id);

    List<Todo> findByDeletedAtIsNull();

    List<Todo> findByMemberIdAndDeletedAtIsNull(Long memberId);

    List<Todo> findByCompletedAndDeletedAtIsNull(Boolean completed);
    
    ...
```

단순한 조건 조합을 처리하는 데에는 충분하게 동작하지만, 조건이 늘어날수록 동일한 패턴의 메서드가 기하급수적으로 증가하는 문제가 있습니다.

예를 들어, 앞으로 태그 필터, 날짜 필터, 정렬 기준 등이 추가되면 메서드 수는 더 증가하게 됩니다.

<br/>

📌 이 부분은 향후 리팩토링이 필요하다고 판단했습니다.

- 조건이 증가해도 메서드 수가 늘어나지 않는 동적 조회 구조 도입

- 서비스 레이어의 분기 로직을 단순화하기 위한 단일 진입점 쿼리 메서드 필요

- 태그, 기간, 정렬 기준이 추가되어도 변경 영향 범위를 최소화할 수 있는 설계가 필요

현재는 요구사항에 맞춰 동작하는 “기능 구현 단계”이지만, 추가 기능이 도입되면 가장 먼저 리팩토링해야 하는 영역이라고 판단하고 있습니다.

<br/>

### 수정(Update)

수정 기능에서는 권한 처리가 가장 중요한 요소였습니다.

권한 로직을 엔티티에 강하게 묶지 않기 위해 `isOwner()` 같은 도메인 메서드를 제공하고, 실제 권한 확인은 서비스 레이어에서 수행하도록 설계했습니다.

- 권한 체크 위치: 서비스 레이어

- 도메인: `isOwner()`로 작성자 여부 판단
- 관리자 정책: 확정 전이므로 현재는 작성자만 수정 가능
- Dirty Checking 활용: 명시적인 `save()` 호출 없이 트랜잭션 내 변경 반영


```java
@Transactional
public void update(Long todoId, TodoUpdateRequest request, Long memberId) {
    Todo todo = todoRepository.findById(todoId)
            .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));

    if (!todo.isOwner(memberId)) {
        throw new TodoException(TodoError.NO_PERMISSION);
    }
    todo.update(request.getTitle(), request.getDueDate(), request.getCompleted());
}
```

<br/>

### 삭제(Delete)

삭제 기능은 현재 `Hard Delete`로 구현했지만, 요구사항에 “삭제한 ToDo를 되돌릴 수 있음”이 언급되어 있어 

필요 시 Soft Delete로 자연스럽게 전환할 수 있도록 구조적 여지를 남겨두었습니다.

- 현재 방식: `Hard Delete`

- Soft Delete 필요성: 추후 ‘복구 기능’ 요구 시 도입 가능
- 권한 처리: 수정과 동일하게 작성자만 삭제 가능
- 트랜잭션: 조회 → 권한 체크 → 삭제까지 하나의 트랜잭션으로 처리

```java
@Transactional
public void delete(Long todoId, Long memberId) {
    Todo todo = todoRepository.findById(todoId)
            .orElseThrow(() -> new TodoException(TodoError.TODO_NOT_FOUND));

    if (!todo.isOwner(memberId)) {
        throw new TodoException(TodoError.NO_PERMISSION);
    }

    todoRepository.delete(todo);
}
```

<br/><br/>

## Tag 기능 – 다대다 관계 처리 방식

Tag 기능은 다양한 방식으로 구현할 수 있어, 관계 설정에 대해 여러 방향을 검토했습니다.

처음에는 `@ManyToMany`를 고려했지만, 중간 테이블을 제어할 수 없고 확장성이 떨어지는 문제가 있었습니다.

양방향 연관관계를 가진 `TodoTag` 엔티티를 검토했지만, 양방향 구조에서 발생하는 `N+1` 문제와 불필요한 참조가 부담이었습니다.

<br/>

최종적으로 선택
```
Todo (태그와 직접 연관관계 없음)
TodoTag (@ManyToOne으로 Todo, Tag를 단방향 참조)
Tag (독립)
```

조회 시 필요한 경우 `todoTagRepository.findByTodoId(todoId)`로 명시적으로 조회하는 방식입니다.

<br/>

이 방식의 장점

- 조회 과정에서 어디에서 쿼리가 발생하는지 예측 가능

- 필요할 때만 Tag를 조회하므로 성능 관리가 용이
- TodoTag 엔티티에 displayOrder, 생성일 등 추가 정보 확장 가능

다만, 직접적으로 조회해야 하므로 코드가 다소 길어지는 단점은 존재합니다.

하지만 전체적인 유지보수성과 확장성을 고려했을 때 장점이 크다고 판단했습니다.

<br/>

## 요청/응답 DTO 설계

Tag 정보를 요청과 응답에서 어떻게 처리할지 고민했습니다.

요청 시에는 ID 목록만 전달하고, 응답에서는 전체 태그 정보를 내려주는 방식으로 설계했습니다.

```java
// 요청 예시
CreateTodoRequest {
    tagIds: [1, 2, 3]
}

// 응답 예시
TodoResponse {
    tags: [
    {id: 1, name: "백엔드", color: "#FF5733"},
    {id: 2, name: "개발", color: "#3498DB"}
    ]
}
```

이 방식은 프론트엔드에서 태그 목록을 먼저 조회한 뒤, 선택한 태그의 ID만 전달하면 되기 때문에 괜찮다고 판단 했으며, 

응답에는 태그 정보 전체를 포함해 추가적인 API 호출을 줄일 수 있습니다.

<br/>

## 삭제 로직 설계

Todo 삭제 시 Tag 관계도 함께 제거해야 하는 상황이 있었습니다.

이를 처리하는 방식으로 `cascade = CascadeType.ALL`을 사용할 수도 있으나, 연쇄 삭제는 코드 가독성과 디버깅 측면에서 불리함이 있다고 판단했습니다.

그리하여 삭제 로직은 명시적으로 처리하는 방식을 선택했습니다.

```java
public void deleteTodo(Long todoId, Long memberId) {
    // 1. 권한 확인
    // 2. TodoTag 삭제
    todoTagRepository.deleteByTodoId(todoId);
    // 3. Todo 삭제
    todoRepository.delete(todo);
}
```

위 처럼 처리 했을때의 장점

- 삭제 경로가 명확해 디버깅이 쉬움

- 의도하지 않은 연쇄 삭제를 방지할 수 있음
- Soft Delete로 전환할 때 구조 수정을 최소화할 수 있음

<br/>

## 예외 처리 구조

예외 처리는 도메인별로 책임을 분리해 일관적인 에러 응답을 제공하는 방향으로 설계했습니다.

```java
CommonError (인터페이스)
  └─ TodoError (enum)
        └─ TodoException (도메인 예외)
              └─ GlobalExceptionHandler
```

- try-catch 사용을 최소화하고 예외 흐름을 명확하게 유지

- 도메인별 에러 코드를 자체적으로 관리할 수 있음
- 전체 API에서 동일한 응답 형식을 유지

<br/>

## 구현 과정에서 발견한 잠재적 문제들

**Tag 삭제 시 참조 문제**
- Tag가 TodoTag에서 참조 중인 상태에서 삭제될 경우 FK 제약으로 에러가 발생합니다.

- 현재는 FK 제약으로 보호하고 있으며, Tag가 실제 삭제 가능한 상태인지 사전 검사하거나 Soft Delete를 적용하는 방식도 고려하고 있습니다.

**Tag 중복 생성 방지**
- Tag는 name에 unique 제약을 걸어 중복을 방지하고 있습니다.

- 하지만 “대소문자 구분”을 어떻게 처리할지는 추후 논의가 필요합니다. 예) “개발” vs “DEVELOPMENT”

**Todo 조회 성능**
- Todo가 많아질 경우 Tag 조회가 반복되어 쿼리가 많아질 수 있습니다.

- 향후 Batch Fetch나 join fetch 전략을 검토할 예정입니다.

**태그 순서 관리**

- 현재는 태그 순서를 보장하지 않습니다.

- TodoTag 엔티티에 displayOrder 필드를 추가하는 방식으로 확장 가능하도록 해두었습니다.

<br/>

## 유지보수성을 고려한 설계 선택

- 메서드명은 간결하고 의미가 명확하도록 작성했습니다.

- 도메인별로 패키지를 분리하여 Controller, Service, Repository, Entity가 한 도메인 내에서 cohesive하게 유지되도록 구성했습니다.

- 중복된 로직은 private 메서드로 정리하되, 불필요한 추상화는 피해 가독성을 유지했습니다.

<br/>

## 협업을 고려한 API 설계

API는 같은 형태로 구성되어 있으며, 도메인별로 일관된 구조를 유지해 협업 시 혼동을 줄이는 방식으로 설계했습니다.


```java
POST   /api/v1/todos
GET    /api/v1/todos
GET    /api/v1/todos/{id}
PUT    /api/v1/todos/{id}
DELETE /api/v1/todos/{id}

POST   /api/v1/tags
GET    /api/v1/tags
GET    /api/v1/tags/{id}
DELETE /api/v1/tags/{id}
```

또한, API 테스트 파일을 함께 제공해 다른 개발자가 빠르게 테스트를 진행할 수 있도록 했습니다.

<br/>

## 태그 필터링 구현 – 설계 과정과 고민

```
요구사항 해석: 사용자, 태그, 완료 여부를 조건으로 ToDo를 필터링할 수 있어야 한다.
```

초기 구현 단계에서는

- 사용자 필터(memberId)

- 완료 여부 필터(completed) 만 지원하고 있었고, 태그(tagId)를 조건으로 필터링하는 기능은 추가로 설계가 필요한 상태였습니다.

- 사용자 필터: 구현 완료

- 완료 여부 필터: 구현 완료

- 태그 필터: 미구현 → 이번 단계에서 설계 및 구현

<br/><br/>

## 태그 필터링 방식 선택

태그를 기준으로 ToDo를 필터링하는 방식은 여러 가지가 가능했기 때문에, 관계 설계와 쿼리 작성 방법을 함께 고려했습니다.

**다대다 매핑을 직접 사용하는 방식은 제외** 처음에는 @ManyToMany 매핑을 이용해 Tag를 직접 Todo에서 참조하고, 조회하는 방법을 고려했습니다.

```java
@ManyToMany
private List<Tag> tags;

// Repository 예시
List<Todo> findByTagsContaining(Tag tag);
```

<br/>

하지만 이 방식은 선택하지 않았습니다.

- 이미 `Todo`와 `Tag` 사이에 `TodoTag`라는 중간 엔티티를 분리한 상태

- 중간 테이블을 세밀하게 제어하기 어렵고, 추후 확장에도 불리함

- 연관관계 설정에 따라 `N+1` 문제 발생 가능성 존재

관계 제어와 확장성을 위해, `@ManyToMany`는 사용하지 않기로 결정했습니다.

<br/>

**TodoTag를 통한 간접 조회 방식 선택**

- 먼저 TodoTag에서 tagId를 기준으로 todoId 목록을 조회하고

- 그 ID 목록을 기준으로 Todo를 한 번 더 조회하는 방식입니다.

```java
// 1단계: 태그로 Todo ID 목록 조회
List<Long> todoIds = todoTagRepository.findTodoIdsByTagId(tagId);

// 2단계: ID 목록으로 Todo 조회
List<Todo> todos = todoRepository.findByIdInAndDeletedAtIsNull(todoIds);
```

- 쿼리가 어느 지점에서 발생하는지 명확하게 파악 가능

- Tag → Todo로 가는 흐름이 분리되어 있어 테스트 및 수정이 용이

- 이후 memberId, completed와의 조합 필터링도 Repository 메서드로 명시적으로 표현 가능

Todo 엔티티에 태그 관련 필드를 추가하지 않고도 유연한 필터링이 가능하다는 점에서 채택했습니다.

<br/>

## 필터 조합 처리 방식

태그 필터가 추가되면서, 실제로는 같은 필터 조합이 가능해졌습니다.

```java
1. tagId만
2. tagId + memberId
3. tagId + completed
4. tagId + memberId + completed
5. memberId만
6. completed만
7. memberId + completed
```

이 조합을 처리하는 방법에 대해서도 몇 가지 선택지를 검토했습니다.

<br/>

**Repository 메서드 조합 방식(현재 방식)**


현재는 Spring Data JPA의 메서드 이름 기반 쿼리 생성을 활용해, 조건 조합별로 Repository 메서드를 나누는 방식을 사용했습니다.

```java
List<Todo> findByIdInAndDeletedAtIsNull(List<Long> ids);
List<Todo> findByIdInAndMemberIdAndDeletedAtIsNull(List<Long> ids, Long memberId);
List<Todo> findByIdInAndCompletedAndDeletedAtIsNull(List<Long> ids, Boolean completed);
List<Todo> findByIdInAndMemberIdAndCompletedAndDeletedAtIsNull(List<Long> ids, Long memberId, Boolean completed);
```

- 메서드 이름만으로 어떤 쿼리가 나가는지 파악 가능

- 별도의 쿼리 문자열 없이 Spring Data JPA가 쿼리를 생성

- 현재 필터 개수(3개: memberId, completed, tagId) 수준에서는 관리 가능

<br/>

다만, 필터 개수가 더 늘어나면 메서드 수가 폭발적으로 증가할 수 있다는 점은 단점으로 인지하고 있습니다.

현재는 “범위 내에서는 허용 가능한 수준”으로 판단하고 있으며, 필터 수가 더 늘어나는 시점에는 

동적 쿼리 기반 구조로 전환하는 것을 고려하게 될 것입니다.

<br/>

### Specification/QueryDSL은 도입하지 않음

Specification 패턴과 QueryDSL도 후보로 검토했습니다.

- Specification: 람다와 조합 기반 동적 쿼리를 작성할 수 있지만, 범위에서 과도한 추상화가 될 수 있고, 팀원이 익숙하지 않을 경우 가독성이 떨어질 수 있다고 판단했습니다.

- 현재 프로젝트의 스코프와 복잡도를 고려할 때, 두 방식을 도입하지 않고 Repository 메서드 조합 방식으로 정리하는 쪽을 선택했습니다.

<br/>

## 잠재적 성능 이슈

현재 구현은 “쿼리 발생 지점이 명확하다”는 장점이 있는 반면, N+1 쿼리 가능성이 있는 패턴도 포함하고 있습니다.

예를 들어, Todo 목록 조회 시 각 Todo에 대한 태그 정보를 조회하고 있습니다.

```java
private TodoResponse buildTodoResponse(Todo todo) {
    List<TodoTag> todoTags = todoTagRepository.findByTodoId(todo.getId());
    return TodoResponse.from(todo, todoTags);
}

// findTodos() 내부
todos.stream()
     .map(this::buildTodoResponse)
     .toList();
```

Todo 1건을 조회할 때는 문제가 되지 않지만, Todo가 10개면 총 11번(목록 1번 + 태그 10번), 100개면 101번의 쿼리가 발생할 수 있는 구조입니다.

현재는 데이터 양을 고려해 이 정도 복잡도는 허용 가능한 수준으로 보고 있지만,

실제 서비스에서 데이터가 많아지는 상황이라면 아래와 같은 개선이 필요합니다.
- Todo ID 목록을 한 번에 모아서 TodoTag를 한 번에 조회하는 방식으로 변경

- Batch Size 설정을 활용한 배치 로딩
- 필요에 따라 Join fetch나 별도의 조회 API로 분리

정리하면, 태그 필터링은 관계 설계(Todo–TodoTag–Tag), 필터 조합 방식(Repository 메서드 조합) 중심으로 설계 및 구현하였습니다.

<br/><br/>

## 순서 변경 기능 구현 – 설계 과정과 고민

순서 변경 기능은 단순히 숫자를 바꾸는 수준이 아니라, 몇 개의 ToDo를 어떤 기준으로 정렬하고, 이를 어떻게 저장하며,

여러 항목을 동시에 수정할 때의 일관성과 성능을 어떻게 보장할 것인지에 대한 고민이 필요한 기능이었습니다.

<br/>

### 순서 저장 방식 선택

가장 먼저 결정해야 했던 것은 순서를 어떤 방식으로 저장할 것인가였습니다.

초기에는 position(1,2,3…)과 같은 단순 정수 필드를 고려했지만, 중간에 새로운 항목이 

삽입되는 경우 하위 항목의 position을 모두 재정렬해야 하는 문제가 있었습니다.

```java
@Column(name = "position")
private Integer position;
```

이 방식은 구현은 단순하지만, 중간 삽입이 잦아지면 대량 업데이트가 발생해 성능 문제가 생길 수 있기 때문에 제외했습니다.

<br/>

그 대신 displayOrder 필드를 큰 간격으로 두는 방식을 선택했습니다.

예를 들어 100, 200, 300으로 저장해두면 150과 같은 값을 삽입해도 기존 값을 대량 변경하지 않아도 됩니다.

```java
@Column(name = "display_order")
private Integer displayOrder;
```

이 방식은 밑에와 같은 이유로 최종 채택했습니다.
- 중간 삽입 시 재정렬 비용이 거의 없음

- 재정렬이 필요한 경우도 간격을 조정해 해결 가능

  
연결 리스트(prev_id, next_id 방식)도 검토했지만, 구조의 복잡도가 커지고 조회 시 재귀적인 탐색이 필요해 현재 프로젝트에서는 적합하지 않다고 판단했습니다.

<br/>

## 정렬 책임을 어디에 둘 것인지

순서 변경 후 목록 조회 시 어떤 기준으로 정렬할지도 중요한 결정이었습니다.

정렬을 Repository 메서드명에 포함시키는 방식도 가능하지만, 

기존에 존재하는 조합(memberId, completed 등)까지 고려하면 정렬 조건까지 포함한 메서드 수가 크게 증가하게 됩니다.

```java
List<Todo> findByMemberIdOrderByDisplayOrderAscIdAsc(Long memberId);
List<Todo> findByMemberIdAndCompletedOrderByDisplayOrderAscIdAsc(...)
// 메서드가 계속 늘어나는 문제 발생
```

<br/>

이를 피하기 위해 정렬 로직은 모두 Service 레이어에서 중앙화했습니다.

```java
private List<Todo> sortTodosByDisplayOrder(List<Todo> todos) {
    return todos.stream()
            .sorted(Comparator.comparing(Todo::getDisplayOrder)
                    .thenComparing(Todo::getId))
            .toList();
}
```
- 정렬 로직이 한 곳에만 존재해 유지보수가 용이함

- Repository는 순수하게 “필터링된 Todo 조회”만 책임짐
- Comparator 체이닝으로 정렬 기준을 쉽게 추가 가능

<br/>

단점은 메모리 정렬이기 때문에 데이터가 많아지면 성능에 영향을 줄 수 있다는 점이지만,

개인 Todo 기준으로 일반적으로 100개 이하라면 충분히 빠른 수준입니다.

향후 필요할 경우 DB 정렬로 전환하거나 페이징 기반 조회로 대응할 수 있습니다.

<br/>

## 순서 변경 API의 형태

순서 변경은 사용자가 여러 항목을 한 번에 드래그 앤 드롭하여 재정렬하는 경우가 많기 때문에,

개별 수정 API를 여러 번 호출하는 방식은 네트워크 비용과 일관성 문제를 발생시킵니다.

이 때문에 순서를 일괄로 변경하는 전용 API를 설계했습니다.

```java
PUT /api/v1/todos/reorder
{
  "orders": [
    {"todoId": 1, "displayOrder": 0},
    {"todoId": 2, "displayOrder": 1},
    {"todoId": 3, "displayOrder": 2}
  ]
}
```

이 방식의 장점

- 한 번의 호출로 여러 항목을 수정할 수 있음

- 트랜잭션 안에서 모두 처리되어 일관성이 보장됨
- 여러 항목의 순서를 변경한다는 의도를 명확하게 전달할 수 있음

또한, Bulk Update를 사용하지 않고 JPA의 Dirty Checking 방식으로 구현해 각 Todo 객체의 displayOrder만 변경하는 형태로 처리했습니다.

<br/><br/>

## 권한 검증 방식

순서를 변경하는 행위는 여러 Todo 항목을 동시에 수정하는 작업이기 때문에, 

모든 Todo에 대해 권한 검증이 필요합니다. 하나라도 권한이 없는 경우 요청 전체를 실패시키는 방향을 선택했습니다.


```java
for (TodoOrderItem orderItem : request.getOrders()) {
Todo todo = findTodoById(orderItem.getTodoId());

    if (!todo.isOwner(memberId)) {
        throw new TodoException(TodoError.FORBIDDEN_TODO_ACCESS);
    }

            todo.updateDisplayOrder(orderItem.getDisplayOrder());
        }
```

- 하나라도 실패하면 전체 순서 변경이 롤백됨

- 클라이언트가 의도한 순서와 실제 저장되는 순서가 불일치하는 문제를 예방
- 도메인 규칙을 정확하게 지킬 수 있음

부분 성공 방식은 사용자 혼란을 초래할 수 있어 제외했습니다.

<br/>

## 구현 과정에서 인지한 문제와 개선 포인트

displayOrder 중복 가능성 현재 displayOrder 필드는 unique 제약을 두지 않았기 때문에 동일 값이 발생할 가능성이 있습니다.

이 경우에는 정렬 시 id 기준으로 한 번 더 정렬해 항상 일관된 순서가 유지되도록 처리했습니다.


```java
.sorted(Comparator.comparing(Todo::getDisplayOrder)
        .thenComparing(Todo::getId))
```

<br/>

다수 항목 업데이트 시 성능 고려 현재는 Dirty Checking으로 UPDATE 쿼리가 항목 수만큼 발생합니다.

```java
-- 조회 1회
SELECT * FROM todo WHERE id IN (1,2,3);

-- UPDATE N회
UPDATE todo SET display_order = 0 WHERE id = 1;
UPDATE todo SET display_order = 1 WHERE id = 2;
```

Todo 개수가 많지 않은 점을 고려해 현재 방식으로 충분하다고 판단했으며, 성능 문제가 발생할 경우 Batch Update로 개선하는 방향을 고려하고 있습니다.

<br/><br/>

## 공유 기능 구현 – 설계 과정과 고민

공유 기능은 단순히 “링크 하나 만들기”가 아니라, 누가, 어떤 방식으로 Todo를 조회할 수 있으며, 

공유 링크를 어떻게 발급·관리할 것인지에 대한 설계가 필요한 기능이었습니다.

<br/>

### 공유 방식 결정

요구사항에서는 “다른 사용자에게 조회만 가능하게”라고만 되어 있어 구체적인 공유 방식이 명확하지 않았습니다. 


사용자 초대 방식(memberId 기반 권한 관리)
- 특정 사용자만 조회 가능하지만, 복잡한 사용자 관리가 필요해 제외했습니다.

전체 공개(isPublic 플래그)
- 보안 문제가 있고, “특정 사용자에게만 공유”라는 의미와 맞지 않아 제외했습니다.

토큰 기반 공유 링크(채택)

```java
- Todo마다 UUID 기반 공유 링크 생성
- 링크를 가진 누구나 조회 가능
- 링크 삭제 = 공유 취소
```

<br/>

## 공유 정보를 어디에 저장할지


Todo 엔티티에 공유 필드를 직접 넣는 방식도 가능했지만, 공유는 Todo의 본질이 아니기 때문에 관심사를 분리하는 것이 더 적절했습니다.

- Todo에 shareToken, expiresAt 등을 넣으면 엔티티가 비대해짐

- 공유하지 않는 Todo도 null 필드를 갖게 되어 설계적으로 불필요한 정보가 포함됨
- 단일 책임 원칙 위반

이 때문에 Share 엔티티를 별도로 두는 구조를 선택했습니다.

```java
@Entity
public class Share {
    private String shareToken;      // UUID
    private Todo todo;              // 공유 대상
    private Long sharedBy;          // 공유한 사용자
    private LocalDateTime expiresAt;
}
```

<br/>

## 공유 링크 생성 방식

링크 생성 시, 세 가지 옵션을 검토했습니다.

- 순차 ID: 추측 가능해 보안 취약 → 제외

- 랜덤 문자열: 가능하지만 중복 체크 필요
- UUID v4(채택)

UUID는 표준이며, 충돌 확률도 사실상 0에 가까워 공유 링크 생성 방식으로 적합합니다.

```java
String shareToken = UUID.randomUUID().toString();
```
DB unique 제약을 통해 이중으로 중복을 보호합니다.

<br/>

## 한 Todo당 링크는 하나만 유지

Todo 하나에 링크 여러 개를 허용할 수도 있었지만, 현재 프로젝트에서는 오히려 복잡함만 증가한다고 판단했습니다.

```java
Todo 1 → Share 1 (하나만 존재)
```

- 공유 취소 = Share 삭제
- 복잡한 공유 정책 관리 불필요

- 요구사항 충족(“공유할 수 있어야 함”)

DB 단에서도 todo_id에 unique index를 적용하면 더욱 안전하게 관리할 수 있습니다.

<br/>

## 공유 링크 API 설계

공유는 Todo의 부가 기능이기 때문에 Todo의 하위 리소스로 구성하는 것이 RESTful하게 맞았습니다.

```java
POST   /api/v1/todos/{todoId}/share       // 공유 생성
DELETE /api/v1/todos/{todoId}/share       // 공유 취소
GET    /api/public/todos/share/{token}    // 공유된 Todo 조회
```

- 사용자는 자신의 Todo에 대해 공유 링크 생성

- 공유된 Todo 조회는 인증없이 접근 가능
- 만료일이 설정되어 있다면 만료 여부를 체크하여 접근 제한

<br/>

## 공유 기능과 관련된 잠재적 문제 인지

**Todo 삭제 시 Share 처리** 

현재 Todo와 Share는 외래키 관계이기 때문에 Todo 삭제 시 Share가 존재하면 FK 제약으로 에러가 발생할 수 있습니다.

- Todo 삭제 시 Share 먼저 삭제(서비스 레이어에서 명시적으로 처리)

- 또는 DB cascade 옵션 적용

현재는 서비스 레이어에서 Share를 함께 삭제하는 방식으로 정리할 계획입니다.

**공유 링크 보안**

- UUID 기반 링크는 추측하기 어렵지만,

- 링크가 유출되면 누구나 Todo를 조회할 수 있습니다.

현재 구조 / UUID 사용 / 만료일(optional) / HTTPS 전제 향후 필요하다면 조회 로그(IP, UserAgent), IP 제한 등을 추가할 수 있습니다.

**공유 링크 생성의 동시성 문제**

- 두 요청이 거의 동시에 들어오면 중복 생성 위험이 있습니다.

- 애플리케이션 레벨: exists 체크
- DB 레벨: todo_id unique 제약으로 2중 보호 가능


<br/><br/>


## 관리자 권한 정책 - 설계 과정과 고민

- 관리자(admin)가 ToDo를 수정, 삭제할 수 있는지 여부가 아직 확정되지 않은 상태였습니다.

- 이 기능은 서비스 운영 과정에서 정책이 바뀔 수 있는 영역이기 때문에, 정책이 변경되더라도 코드를 최소한으로 수정하고 유지보수할 수 있는 구조가 필요했습니다.

- 이를 위해 권한 체크 로직을 전략 패턴(Strategy Pattern) 으로 분리하여 설계했습니다.


<br/>



**권한 검사 인터페이스 정의**

우선 모든 권한 검사는 하나의 인터페이스로 추상화하여,
구현체만 교체하면 정책이 변경되는 방식으로 구조를 잡았습니다.

```java
public interface TodoPermissionChecker {
    boolean canModify(Todo todo, Member member);
    boolean canDelete(Todo todo, Member member);
}
```
- TodoService는 이 인터페이스에만 의존하며 실제 정책 구현은 별도의 클래스에서 담당합니다.

이렇게 하면 “정책 변경 = 구현체 교체”만으로 해결할 수 있어 코드 수정 범위를 최소화할 수 있습니다.

<br/>

## 두 가지 정책 구현체

요구사항에서 제시된 두 가지 관리자 정책을 그대로 구현체로 분리했습니다.

(1) 본인만 수정/삭제 가능(정책 2)


```java
@Component
public class OwnerOnlyPermissionChecker implements TodoPermissionChecker {

    @Override
    public boolean canModify(Todo todo, Member member) {
        return todo.isOwner(member.getId());
    }

    @Override
    public boolean canDelete(Todo todo, Member member) {
        return todo.isOwner(member.getId());
    }
}
```

관리자라도 자신이 생성한 Todo만 수정·삭제할 수 있는 정책입니다.
보안성이 높고, “개인 중심 서비스”에 적합한 구조입니다.

<br/>

(2) 관리자는 모든 Todo를 수정/삭제 가능(정책 1)

```java
@Component
public class AdminAllAccessPermissionChecker implements TodoPermissionChecker {

    @Override
    public boolean canModify(Todo todo, Member member) {
        return member.isAdmin() || todo.isOwner(member.getId());
    }

    @Override
    public boolean canDelete(Todo todo, Member member) {
        return member.isAdmin() || todo.isOwner(member.getId());
    }
}
```

관리 기능이 필요한 서비스라면 이 정책이 더 적합합니다.

<br/>

## 정책 선택은 설정(application.yml)으로 제어

코드가 아닌 설정으로 정책을 선택할 수 있도록 구성했습니다.

```java
todo:
permission:
policy: owner-only   # 기본값
    # policy: admin-all-access
```

환경별(개발/운영)로 다른 정책을 적용하는 것도 가능합니다.

<br/>

## Service 계층에서의 사용 구조

TodoService는 어떤 정책이 적용되는지 알 필요가 없고,
현재 설정에 맞는 구현체만 주입받아 사용하도록 구성했습니다.

```java
private TodoPermissionChecker getPermissionChecker() {
    if ("admin-all-access".equals(permissionPolicy)) {
        return adminAllAccessPermissionChecker;
    }
    return ownerOnlyPermissionChecker;
}
```

<br/>

이후 모든 수정/삭제 로직의 형식으로 권한을 검증합니다.


```java
TodoPermissionChecker checker = getPermissionChecker();
if (!checker.canModify(todo, member)) {
        throw new TodoException(TodoError.FORBIDDEN_TODO_ACCESS);
}
```

정책이 바뀌더라도 여기에는 어떠한 변경도 필요하지 않습니다.

<br/>

## 설계 의도와 장점

- 전략 패턴(Strategy Pattern) -> 권한 로직을 알고리즘으로 보고, 필요에 따라 교체 가능

- OCP(개방-폐쇄 원칙) -> 새 정책 추가 시 기존 코드는 변경하지 않음(확장만)
- DIP(의존성 역전 원칙) -> Service는 구체 클래스가 아닌 인터페이스에 의존
- 정책 변경 비용 최소화 -> application.yml 변경만으로 정책 전환 가능

