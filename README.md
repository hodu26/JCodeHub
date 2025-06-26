# JCode WebIDE Backend


## 📌 프로젝트 개요

■ **전체 프로젝트**  
- 교육용 WebIDE 제공을 통해 교수자·학생 누구나 어디서든 웹으로 접근 가능  
- 교수자는 각 학생의 과제 수행도를 실시간 대시보드 및 그래프 분석으로 확인  
- 과제 난이도 조정, LLM 사용·이상 행위 탐지 등 학습 품질 관리 시스템 구현  

■ **내 세부 프로젝트**  
- 교육용 WebIDE와 분석 시스템·프론트를 연결하는 미들웨어 백엔드 개발  
- WebIDE(VNC 포함) 컨테이너의 동적 생성·삭제 및 프록시 서비스 구축  
- Keycloak 기반 인증 강화, JWT·Redis 연계 세션 관리로 보안성 확보  


---


## 🎯 전체 역할 분담

| 역할                              | 담당 내용                                                                                                                                                                |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ◆ PM (1)                          | 프로젝트 총괄 및 일정·자원 관리                                                                                                                                           |
| ◆ Front (1)                       | UI 구성 및 Backend 연계                                                                                                                                                  |
| ◆ **Main Backend & Proxy (2) - ME**   | - 분석 시스템과 프론트 연동 미들웨어 설계·구현<br>- Keycloak 인증 → 자체 JWT 발급·Refresh Token Rotate 구현<br>- UUID 기반 쿠키/파라미터 전송으로 세션 관리 강화<br>- WebIDE(VNC) 컨테이너 생성·삭제 서비스 분리 |
| ◆ Watch WebIDE & Analyze (2)      | 사용자별 활동 로그 수집·분석, 이상 행위 탐지 서비스 개발                                                                                                                   |


---


## 🏗 아키텍처
![JCode Backend Architecture](https://github.com/user-attachments/assets/e605df24-a650-42f5-bc83-2ba28280aadc)

- WebIDE 컨테이너는 MS의 CodeServer(`coder/code-server`) 이미지를 활용·커스터마이징





## 🚀 마이크로서비스 구성

- **Backend**  
  사용자 인증·인가, 강의·사용자 정보 DB 관리, 마이크로서비스 간 통신 보안 처리  
- **Container Generator**  
  Kubernetes API에 직접 YAML 전송 → WebIDE 컨테이너 동적 생성·삭제  
- **Proxy Server**  
  Redis 연계 이중 인증 프록시 → WebSocket·VNC·파일 트래픽 재검증, 부하 분산  
- **Namespace Management**  
  강의별 네임스페이스 생성·삭제, 네트워크 정책·HPA 설정으로 자원·트래픽 관리


---


## 🔄 Sequence Diagram

![Proxy Sequence Diagram](https://github.com/user-attachments/assets/615a57c6-6064-4db6-ab1c-09380b57c3ec)


## 데이터 흐름
1. 클라이언트 → Backend 로그인 요청
2. Backend ↔ Redis → 토큰/세션 발급
3. Generator에 컨테이너 생성 명령 → Kubernetes 스케줄링
4. 생성된 Pod의 엔드포인트를 Proxy로 라우팅
5. Proxy가 트래픽 재검증 후 WebIDE(VPN) 서비스 제공
6. 이후 사용자는 외부에서 Proxy를 경유하여 WebIDE(VPN) 접근


---


## 🔄 WebIDE(VNC) Demo

![demo](https://github.com/user-attachments/assets/a200b3c8-9cb2-47ff-a1c3-d567eb49120a)


---


## 📝 Lessons Learned

✔ **YAML 직접 전송 프로비저닝** → 컨테이너 배포 지연 최소화  
✔ **강의별 네임스페이스 분리** → 격리성과 관리 효율성 동시 확보 (HPA·네트워크 정책)  
✔ **Redis 연계 이중 인증 프록시** → 초기 인증 후 세션 재검증으로 부하 분산·안정적 인증  





## 📊 Performance

| 구성 요소           | 지표                  | 결과             |
|---------------------|-----------------------|------------------|
| • Container Generator | 평균 배포 시간        | 7.40 ± 0.34 초  |
|                     | 평균 삭제 시간        | 2.70 ± 0.22 초  |
| • Proxy Server        | 평균 CPU 사용률       | 0.67%           |
|                     | 메모리 사용량         | 70 MiB – 90 MiB |
|                     | Event Loop Lag        | < 10 ms         |
| • Backend             | JVM Heap 사용률      | 3.70%           |
|                     | CPU 사용률           | 0.13%           |
|                     | Load Average          | 약 2.12         |
| • 실서비스 적용 기간  | 배포 지연·인증 실패  | 거의 없음       |

> _2025.03 ~ 현재 진행 중_


---


## ⚔ 주요 쟁점 및 해결

1. **Keycloak 토큰 직접 사용 vs 자체 JWT 발급**  
   - MSA 서비스 구축 시 각 서비스에서 Keycloak 토큰 공유·만료 확인 부담 → 자체 JWT 발급 방식 채택  
2. **Keycloak 연계 위치 논의**  
   - Keycloak과 프론트 vs 백엔드 연계 → 인증·인가 일원화 위해 메인 백엔드 연동으로 합의  





## 🛠 Troubleshooting

1. **Proxy 서버 인증 강화**
   - 문제: 프록시 서버에서 WebIDE로 프록시 할 시 초기 인증 이후 웹소켓은 권한검증을 추가로 하고 있지 않은 것을 확인
   - 해결: 웹소켓 포함 모든 트래픽 권한 검증 추가 
3. **WebIDE 새로고침 시 강의 꼬임**
   - 문제: UUID를 파라미터로만 넘길 시, 초기 검증 이후 웹소켓 및 정적 파일에서 권한 검증 불가
           UUID를 쿠키로만 넘길 시, 2개 이상의 WebIDE(VNC) 동시 사용 시 웹소켓 및 정적 파일의 경로가 꼬여 오류 발생
   - 해결: UUID를 파라미터 + 쿠키 동시 전송, 새로고침 시 쿠키 동기화로 문제 해결  


---

## 📎 추가 자료

- [KCC 학부생 논문 (최종심사용)](KCC_최종심사용논문-1.pdf)  
- **GitHub**  
  - Main Backend: https://github.com/hodu26/JCodeHub  
  - Micro Services: https://github.com/hodu26/JCodeMS  
