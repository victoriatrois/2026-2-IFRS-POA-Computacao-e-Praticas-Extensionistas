export type RubricLevel = {
  value: number;
  label: string;
  description: string;
};

export type EvaluationCriterion = {
  id: string;
  question: number;
  title: string;
  prompt: string;
  levels: RubricLevel[];
};

const levelNames = ["INSATISFATÓRIO", "REGULAR", "ADEQUADO", "MUITO BOM", "EXCELENTE"];

function levels(descriptions: [string, string, string, string, string]): RubricLevel[] {
  return descriptions.map((description, index) => ({
    value: index + 1,
    label: levelNames[index],
    description,
  }));
}

export const evaluationCriteria: EvaluationCriterion[] = [
  {
    id: "article-clarity", question: 1, title: "Resumo/Artigo: clareza e rigor científico",
    prompt: "Avalie se o resumo/artigo apresenta clareza, organização textual e rigor científico, atendendo às normas gramaticais.",
    levels: levels([
      "Não segue o modelo proposto. Texto confuso, incompleto ou com erros graves de estrutura e linguagem. Ausência de informações essenciais do projeto.",
      "Apresenta estrutura básica, porém com falhas de organização, clareza ou adequação às normas. Informações importantes estão incompletas ou pouco desenvolvidas.",
      "Atende ao modelo proposto de forma geral. Texto compreensível, com descrição básica do projeto e poucos problemas de organização ou linguagem.",
      "Bem estruturado, claro e completo. Apresenta boa organização das ideias, correção gramatical e contempla todos os elementos solicitados no modelo.",
      "Excelente qualidade textual e rigor científico. Apresenta organização lógica consistente, linguagem precisa, clareza na comunicação e total aderência ao modelo, incluindo todos os elementos obrigatórios bem integrados.",
    ]),
  },
  {
    id: "article-format", question: 2, title: "Resumo/Artigo: adequação ao modelo oficial",
    prompt: "Avalie se o resumo/artigo atende o modelo oficial (formatação, número de páginas, estrutura, descrição do projeto e inserção de elementos obrigatórios como link e/ou QR code do vídeo).",
    levels: levels([
      "Não segue o modelo proposto.",
      "Informações importantes estão incompletas ou pouco desenvolvidas.",
      "Atende ao modelo proposto de forma geral.",
      "Contempla todos os elementos solicitados no modelo.",
      "Excelente qualidade, incluindo todos os elementos obrigatórios bem integrados.",
    ]),
  },
  {
    id: "objective", question: 3, title: "Objetivo",
    prompt: "Avalie se o trabalho apresenta um objetivo claro, específico e coerente com o problema investigado, considerando sua relevância científica e a possibilidade de verificação por meio dos resultados obtidos.",
    levels: levels([
      "Objetivo inexistente, incoerente ou desconectado do problema apresentado, não permitindo compreender o propósito do projeto.",
      "Objetivo pouco claro, genérico ou mal formulado, com relação superficial ao problema investigado.",
      "Objetivo claro e relacionado ao tema, permitindo compreensão da proposta, ainda que com pouca especificidade.",
      "Objetivo bem definido, específico e coerente com o problema, orientando adequadamente o desenvolvimento do trabalho.",
      "Objetivo muito claro, específico e mensurável, com forte consistência científica e alinhamento direto com os resultados esperados.",
    ]),
  },
  {
    id: "project-stage", question: 4, title: "Fase do projeto",
    prompt: "Avalie, com base nas etapas desenvolvidas e nas evidências apresentadas, o nível de maturidade do projeto, considerando planejamento, execução e validação dos resultados.",
    levels: levels([
      "Apenas a ideia foi apresentada, sem evidências de desenvolvimento, planejamento estruturado ou execução.",
      "Projeto em fase inicial, com poucas evidências de execução ou registros do processo, apresentando desenvolvimento ainda incipiente.",
      "Projeto em desenvolvimento, com evidências parciais das etapas realizadas, incluindo testes iniciais ou construção em andamento.",
      "Projeto finalizado, com execução completa das etapas propostas e apresentação de resultados claros e coerentes com os objetivos.",
      "Projeto em nível avançado, com validação consistente dos resultados, aprofundamento das análises, refinamento da solução e possíveis desdobramentos ou aplicações.",
    ]),
  },
  {
    id: "prototype", question: 5, title: "Protótipo/experimento",
    prompt: "O protótipo/experimento foi apresentado, explicado e encontra-se na fase:",
    levels: levels([
      "Não apresentado. Somente foi apresentada a ideia.",
      "Inicial e pouco funcional.",
      "Funcional, mas em desenvolvimento.",
      "Funcional e bem explicado.",
      "Alta qualidade, validado, robusto e replicável.",
    ]),
  },
  {
    id: "development", question: 6, title: "Programação/desenvolvimento",
    prompt: "Avalie se a solução técnica está adequada ao objetivo do projeto, considerando lógica de funcionamento, organização do código/sistema, integração entre componentes, nível de autonomia e evidências de testes e validação.",
    levels: levels([
      "Ausente ou não funcional. Não há evidências de desenvolvimento técnico ou o sistema não se relaciona com o objetivo do projeto.",
      "Desenvolvimento inicial com falhas significativas. Apresenta lógica incompleta, erros frequentes ou dependência excessiva de intervenção externa para funcionar.",
      "Sistema funcional em nível básico. Atende parcialmente ao objetivo, com estrutura simples e pouca otimização. Evidências limitadas de testes.",
      "Bem estruturado, funcional e coerente com o objetivo. Apresenta organização do código/sistema, integração adequada entre componentes e evidências consistentes de testes e ajustes.",
      "Desenvolvimento avançado, otimizado e robusto. Demonstra autonomia do sistema, eficiência na solução proposta, refinamento técnico, documentação clara e validação consistente por meio de testes e replicação.",
    ]),
  },
  {
    id: "results", question: 7, title: "Resultados e conclusões",
    prompt: "Avalie se os resultados apresentados estão coerentes com os objetivos propostos, considerando a consistência da análise, a qualidade das evidências e o potencial de gerar melhorias, aplicações ou impactos.",
    levels: levels([
      "Resultados inexistentes, incoerentes ou desconectados dos objetivos. Não há análise ou conclusões consistentes.",
      "Resultados pouco claros ou com baixa relação com os objetivos. Análises superficiais ou incompletas, com conclusões pouco fundamentadas.",
      "Resultados relacionados aos objetivos, com análise básica e conclusões coerentes, ainda que pouco aprofundadas.",
      "Resultados consistentes e bem analisados, com conclusões claras e alinhadas aos objetivos, evidenciando potencial de aplicação ou melhoria.",
      "Resultados robustos, com análise aprofundada e interpretação crítica, apresentando conclusões bem fundamentadas e alto potencial de impacto, inovação ou aplicação prática.",
    ]),
  },
  {
    id: "robotics-pillars", question: 8, title: "Pilares da robótica",
    prompt: "Avalie se o projeto contempla os três pilares fundamentais da robótica: mecânica, eletrônica e programação, considerando também o nível de integração entre eles.",
    levels: levels([
      "Não apresenta nenhum dos pilares da robótica.",
      "Apresenta apenas um dos pilares (mecânica ou eletrônica ou programação), de forma isolada.",
      "Apresenta dois pilares, com integração parcial entre eles.",
      "Apresenta os três pilares (mecânica, eletrônica e programação), com integração adequada e funcionamento coerente.",
      "Apresenta os três pilares de forma integrada, com alto nível de domínio técnico, eficiência do sistema e coerência com os objetivos do projeto.",
    ]),
  },
  {
    id: "innovation", question: 9, title: "Inovação",
    prompt: "Avalie o grau de criatividade, originalidade e aplicabilidade das soluções propostas, considerando especialmente o uso racional e eficiente de recursos naturais e tecnológicos.",
    levels: levels([
      "Não apresenta inovação. Solução comum, reproduzida ou sem contribuição original para o problema proposto.",
      "Baixo nível de inovação. Apresenta pequenas adaptações de soluções já conhecidas, com pouca originalidade.",
      "Apresenta algum grau de inovação, com elementos criativos ou adaptações relevantes ao contexto do problema.",
      "Solução criativa e original, com boa aplicabilidade e uso adequado de recursos tecnológicos e/ou naturais.",
      "Solução altamente inovadora, original e bem fundamentada, com uso eficiente de recursos, forte potencial de aplicação e contribuição relevante para a área ou para a sociedade.",
    ]),
  },
  {
    id: "presentation", question: 10, title: "Apresentação",
    prompt: "Avalie a qualidade da apresentação do projeto (banner presencial e exposição oral), considerando clareza das ideias, objetividade, domínio do conteúdo, postura dos estudantes, consistência das informações e evidências de autoria do trabalho.",
    levels: levels([
      "Apresentação confusa, com falta de domínio do conteúdo. Informações inconsistentes ou incoerentes, sem evidências de autoria dos estudantes.",
      "Apresentação com baixa clareza e organização. Domínio limitado do conteúdo e pouca segurança na exposição. Evidências frágeis de autoria.",
      "Apresentação clara e compreensível, com domínio básico do conteúdo. Os estudantes demonstram participação no projeto, ainda que com limitações.",
      "Apresentação bem estruturada, clara e objetiva, com bom domínio do conteúdo e postura adequada. Evidências consistentes de autoria dos estudantes.",
      "Apresentação segura, clara e envolvente, com excelente domínio do conteúdo, argumentação consistente e forte evidência de autoria e protagonismo dos estudantes.",
    ]),
  },
  {
    id: "teacher", question: 11, title: "Professor",
    prompt: "Avalie a postura do professor durante a apresentação, considerando o nível de autonomia dos estudantes e o respeito ao protagonismo estudantil.",
    levels: levels([
      "Professor conduz a apresentação ou responde no lugar dos estudantes, comprometendo a autonomia.",
      "Professor interfere com frequência, direcionando respostas ou complementando excessivamente a fala dos alunos.",
      "Professor mantém postura de apoio pontual, atuando como ouvinte na maior parte do tempo.",
      "Professor intervém minimamente, garantindo que os estudantes conduzam a apresentação com autonomia.",
      "Total protagonismo dos estudantes. Professor atua apenas como observador, evidenciando autonomia, segurança e autoria dos alunos.",
    ]),
  },
  {
    id: "social-environmental", question: 12, title: "Relevância ambiental e social",
    prompt: "Avalie se o projeto aborda um problema real e significativo, considerando seu potencial de impacto positivo para a sociedade e/ou para o meio ambiente.",
    levels: levels([
      "Problema pouco relevante ou desconectado de demandas reais da sociedade ou do meio ambiente.",
      "Baixa relevância ou impacto limitado, com pouca relação com necessidades concretas.",
      "Problema relevante, com potencial básico de contribuição social ou ambiental.",
      "Problema relevante e bem contextualizado, com bom potencial de impacto positivo e aplicação prática.",
      "Problema altamente relevante, com forte potencial de impacto social e/ou ambiental, demonstrando contribuição significativa para a comunidade ou sustentabilidade.",
    ]),
  },
  {
    id: "interdisciplinarity", question: 13, title: "Interdisciplinaridade",
    prompt: "Avalie se o projeto integra diferentes áreas do conhecimento, promovendo conexões entre ciência, tecnologia e meio ambiente.",
    levels: levels([
      "Não há integração entre áreas do conhecimento.",
      "Integração muito limitada ou superficial entre áreas.",
      "Integração básica entre duas áreas do conhecimento.",
      "Boa integração entre diferentes áreas, com conexões claras e coerentes.",
      "Integração profunda e consistente entre múltiplas áreas, demonstrando visão sistêmica e abordagem interdisciplinar sólida.",
    ]),
  },
];

export type DemoProject = {
  id: number;
  stand: string;
  name: string;
  team: string;
  institution: string;
  category: string;
  location: string;
  pdfUrl: string;
  videoUrl: string;
  materials: { article: boolean; video: boolean };
  completed: boolean;
};

// TODO: Remove these fake projects when the evaluation page is connected to the real API.
export const mockProjects: DemoProject[] = [
  { id: 9101, stand: "A01", name: "Robô de Precisão para Agricultura Familiar", team: "AgroTech Júnior", institution: "IFRS Campus Bento Gonçalves", category: "WRO Future Engineers", location: "Bento Gonçalves/RS", pdfUrl: "https://example.com/artigo-roboag.pdf", videoUrl: "https://www.youtube.com/", materials: { article: true, video: true }, completed: false },
  { id: 9102, stand: "A02", name: "Impressora Braille de Baixo Custo", team: "IncluTech", institution: "ETEC Fernando Febeliano", category: "Mostra Livre", location: "Carapicuíba/SP", pdfUrl: "https://example.com/artigo-braillebot.pdf", videoUrl: "https://www.youtube.com/", materials: { article: true, video: true }, completed: false },
  { id: 9103, stand: "A03", name: "Monitor Autônomo de Qualidade da Água", team: "MarTech Robotics", institution: "CEFET-MG Contagem", category: "RoboCup Junior", location: "Contagem/MG", pdfUrl: "https://example.com/artigo-oceanbot.pdf", videoUrl: "https://www.youtube.com/", materials: { article: true, video: false }, completed: false },
  { id: 9104, stand: "A04", name: "IA para Otimização de Semáforos", team: "SmartCity Makers", institution: "IFPR Campus Foz do Iguaçu", category: "FLL Challenge", location: "Foz do Iguaçu/PR", pdfUrl: "https://example.com/artigo-trafficsense.pdf", videoUrl: "https://www.youtube.com/", materials: { article: true, video: true }, completed: false },
  { id: 9105, stand: "A05", name: "Classificador Automatizado de Resíduos", team: "GreenBots", institution: "SENAI Chapecó", category: "WRO Future Engineers", location: "Chapecó/SC", pdfUrl: "https://example.com/artigo-ecosort.pdf", videoUrl: "https://www.youtube.com/", materials: { article: false, video: true }, completed: false },
];
