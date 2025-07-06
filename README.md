# Anne Diary

Android 2명, Backend 1명, AI 1명, Design 1명 / 2023.11 ~

사진과 간단한 설명을 입력하면, ChatGPT를 활용해 그날의 일기를 자동으로 작성해주는 일기 생성 서비스

## 프로젝트 개요
> 사용자 이미지 + 설명 입력 → ChatGPT 프롬프트 전송 → 실시간 일기 스트리밍 → DB 저장 및 사용자 응답

해당 프로젝트는 위 과정을 거쳐 사용자가 업로드한 **사진과 간단한 설명만으로** 하루를 담은 일기를 자동으로 작성해주는 앱 서비스입니다.

---
## 주요 기능
| 기능                | 설명                                                                                |
|-------------------|-----------------------------------------------------------------------------------|
| 일기 자동 생성          | 이미지와 설명을 기반으로 ChatGPT API에 프롬프트 요청하여 일기를 자동 생성                                    |
| 실시간 일기 생성 과정 제공   | SseEmitter를 사용한 Server-Sent Events로 사용자가 일기 생성 과정을 실시간으로 확인 가능                    |
| Google FedCM 인증   | Google 계정을 이용한 간편한 로그인 인증 및 JWT 기반 사용자 세션 관리                                      |
| 이미지 업로드           | AWS S3에 이미지 업로드 및 URL 저장 처리                                                       |
| 에러 로그 디스코드 알림     | Logback + JDA를 활용하여 에러 발생 시 팀 Discord 채널로 자동 알림 전송                                |
| 무중단 배포 및 보안 HTTPS | Docker + NginX + GitHub Actions + Let's Encrypt를 이용한 Blue-Green 무중단 배포 및 HTTPS 적용 |

---
## 빌드 및 실행
```
./gradlew build
./gradlew bootRun
```
---

## 시스템 아키텍처
![Image](https://github.com/user-attachments/assets/cc71bcdf-4a8c-4f60-a2d5-df265e702df6)

## 사용 기술
- **Spring Boot**: REST API 서버, SSE 처리
- **ChatGPT API**: 일기 자동 생성 프롬프트 전송
- **SseEmitter (SSE)**: 일기 생성 과정 실시간 스트리밍
- **MySQL**: 일기/사용자 정보 저장
- **Redis**: JWT 세션 관리
- **Google FedCM + JWT**: 인증 시스템
- **AWS S3**: 사용자 이미지 저장
- **Docker + NginX + GitHub Actions**: Blue-Green 무중단 배포
- **JDA (Java Discord API)**: 에러 발생 시 팀 디스코드 채널 알림
- **Logback**: 로그 파일 기록 및 디스코드 전송용 포맷팅 설정  
