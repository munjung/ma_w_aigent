
## MA Hub 프로젝트 주의사항

### 빌드
- Maven 빌드: mvn test -pl 모듈명
- 전체 빌드: mvn install -DskipTests

### 코드 품질
- assert 대신 Objects.requireNonNull 사용
- 테스트는 반드시 PASS 확인 후 완료 선언

### Phase 진행 규칙
- 각 Phase 완료 전 전체 테스트 통과 필수
- 실수 발견 시 이 파일에 룰 추가할 것

## 주의사항
- 테스트는 반드시 40개 이상 유지
- assert 대신 Objects.requireNonNull 사용
- Maven 빌드: mvn test -pl 모듈명