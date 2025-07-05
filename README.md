# JCode WebIDE Backend


## 📌 프로젝트 개요

> **이 프로젝트**는 Kubernetes on OpenStack 프로젝트의 연장선입니다.

 이 프로젝트는 학생들이 IP:Port로 직접 WebIDE에 접근하고, 컨테이너 생성·삭제를 수동 요청해야 하는 불편함이 있어 이를 해결하는 동시에 학생들의 데이터를 분석하기 위해 기획되었습니다.

■ **전체 프로젝트**  
- 교육용 WebIDE 제공을 통해 교수자·학생 누구나 어디서든 웹으로 접근 가능  
- 교수자는 각 학생의 과제 수행도를 실시간 대시보드 및 그래프 분석으로 확인  
- 과제 난이도 조정, LLM 사용·이상 행위 탐지 등 학습 품질 관리 시스템 구현  

■ **내 세부 프로젝트**  
- 교육용 WebIDE와 분석 시스템·프론트를 연결하는 미들웨어 백엔드 개발  
- WebIDE(VNC 포함) 컨테이너의 동적 생성·삭제 및 프록시 서비스 구축  
- Keycloak 기반 인증 강화, JWT·Redis 연계 세션 관리로 보안성 확보  


---

## 🔧 기술 스택
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat-square&logo=kubernetes&logoColor=white)
![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=flat-square&logo=kotlin&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-43853D?style=flat-square&logo=node.js&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=flat-square&logo=fastapi&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb&logoColor=white)
![MSA](https://img.shields.io/badge/MSA-Microservices-7D7D7D?style=flat-square)

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

> WebIDE 컨테이너는 MS의 CodeServer(`coder/code-server`) 이미지를 활용·커스터마이징

- **Backend :** 인증·DB·API 게이트웨이
- **Container Generator :** Kubernetes API → 컨테이너 동적 생성·삭제
- **Proxy Server :** Redis 연계 프록시 → WebSocket·VNC 트래픽 재검증, 부하 분산
- **Namespace Management :** 강의별 네임스페이스,네트워크 정책·HPA 설정으로 자원·트래픽 관리
- **스토리지(PV) :** RWX 볼륨 프로비저닝(Longhorn), 컨테이너 데이터 중앙 관리
- **Redis :** 사용자 정보 전달용 캐시, Spring Session 관리, JWT 토큰 블랙리스트 운영


---


## 🎬 WebIDE(VNC) Demo

![demo](https://github.com/user-attachments/assets/a200b3c8-9cb2-47ff-a1c3-d567eb49120a)
> _2025.03 ~ 현재 진행 중_


---


## 📝 Lessons Learned

✔ **YAML 직접 전송 프로비저닝** → 컨테이너 배포 지연 최소화  
✔ **강의별 네임스페이스 분리** → 격리성과 관리 효율성 동시 확보 (HPA·네트워크 정책)  
✔ **Redis 연계 이중 인증 프록시** → 초기 인증 후 세션 재검증으로 부하 분산·안정적 인증  


---

## 📎 추가 자료

- [KCC 학부생 논문 (최종심사용)](KCC_최종심사용논문-1.pdf)  
- **GitHub**  
  - Main Backend: https://github.com/hodu26/JCodeHub  
  - Micro Services: https://github.com/hodu26/JCodeMS  
