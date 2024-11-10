# KnuSearch [AI를 이용한 강대 홈페이지 통합 검색 성능 향상] 　   
이 작품은 2023 강남대학교 **졸업작품전시회**에서 **우수상**을 수여한 프로젝트입니다.

![K_Expo-전시회-포스터-양식_복사본-001 (4)](https://github.com/inpink/Knu_Search_Web_Project/assets/108166692/8b142fb2-2ab3-4f5f-a9d5-e421c8405529)
=> 현재는 CNN-Transformer 모델로 변경하여 성능 향상　   
　    
    　    
> 작품 소개

1. 강남대학교의 여러 홈페이지들의 게시글을 한 곳에서 검색할 수 있습니다. 
2. 사용자가 입력한 검색어와 관련도가 높은 순서대로 보여줍니다.
3. 사용자의 검색어가 어떤 분류에 해당하는지 파악하고, 같은 분류에 속하는 게시글을 더 관련도 높은 게시글로 볼 수 있습니다. 
4. 홈페이지 게시글 데이터의 사용을 위해 Crawling을 이용했으며, Spring Boot 환경에서 MySQL과 ElasticSearch Engine을 제어합니다.
5-1. 단어 검색과 검색 결과 정렬 설정은 ElasticSearch을 사용합니다. ELK Stack을 이용하여 관리합니다. 
5-2. 배포 환경에서 메모리 이슈로 ELK Stack을 사용할 수 없을 경우, MySQL에서 받아온 게시글들에 유사도 알고리즘을 적용하여 직접 검색 성능을 향상시킵니다.
6. AI(딥러닝, 자연어처리)를 통해 모든 게시글을 분류합니다.
  - Crawling한 데이터를 Word2Vec를 이용하여 단어들을 벡터화합니다.
  - 벡터화된 데이터는 CNN-Transformer 알고리즘을 거칩니다. 
  - CNN을 통해 문장에서 분류의 패턴에 해당될 수 있는 지역적인 중요 특징들을 파악하고, Transformer을 이용하여 중요 특징들을 연결하며 전반적인 문장의 문맥을 파악하여 최종적으로 분류합니다.

　   
> 사용 기술과 버전

Spring boot: 3.1.3 　   
JPA: 3.1.3 　   
JDK: 17 　   
MySQL: 8.0.3 　   
Ubuntu: 22.04 　   
Docker: 24.0.6 　   
flask: 3.0.2　
(Morpheme Analyzer: nori) 　
 　   
jQuery: 3.6.0 　   
Bootstrap: 5.0.2 　   
Thymeleaf: 3.1.2 　   
 　   
AI Algorithm: Word2Vec, CNN-Transformer 　   

AWS EC2: t2.micro
 　   
 　   

