INSERT INTO categories (id, slug, name)
VALUES
    (md5('code-arena:category:oop')::uuid, 'OOP', 'Orientacao a objetos'),
    (
        md5('code-arena:category:collections')::uuid,
        'COLLECTIONS',
        'Collections'
    ),
    (
        md5('code-arena:category:streams')::uuid,
        'STREAMS',
        'Streams e programacao funcional'
    );

CREATE TEMPORARY TABLE seed_questions (
    seed_key VARCHAR(10) PRIMARY KEY,
    difficulty VARCHAR(20) NOT NULL,
    statement TEXT NOT NULL,
    explanation TEXT NOT NULL,
    correct_order SMALLINT NOT NULL,
    alternative_1 TEXT NOT NULL,
    alternative_2 TEXT NOT NULL,
    alternative_3 TEXT NOT NULL,
    alternative_4 TEXT NOT NULL,
    category_slugs VARCHAR(50)[] NOT NULL
) ON COMMIT DROP;

INSERT INTO seed_questions (
    seed_key,
    difficulty,
    statement,
    explanation,
    correct_order,
    alternative_1,
    alternative_2,
    alternative_3,
    alternative_4,
    category_slugs
)
VALUES
    (
        'B01',
        'BEGINNER',
        'Qual interface e a raiz principal da hierarquia de listas e conjuntos em Java?',
        'Collection e a interface base de List e Set e estende Iterable.',
        2,
        'Map',
        'Collection',
        'Stream',
        'Comparator',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'B02',
        'BEGINNER',
        'Por que equals e hashCode sao importantes ao armazenar objetos em um HashSet?',
        'HashSet usa hashCode para localizar candidatos e equals para confirmar igualdade.',
        3,
        'Para ordenar os elementos',
        'Para converter objetos em streams',
        'Para identificar elementos iguais',
        'Para tornar a classe abstrata',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'B03',
        'BEGINNER',
        'Qual caracteristica diferencia uma List de um Set?',
        'List preserva posicoes e aceita elementos duplicados; Set nao aceita duplicatas.',
        1,
        'List aceita elementos duplicados',
        'List aceita somente numeros',
        'Set sempre ordena os elementos',
        'Set usa indices numericos',
        ARRAY['OOP', 'COLLECTIONS']
    ),
    (
        'B04',
        'BEGINNER',
        'Qual implementacao e apropriada para uma lista redimensionavel com acesso por indice?',
        'ArrayList implementa List sobre um array dinamico e oferece acesso por indice.',
        4,
        'HashMap',
        'TreeSet',
        'ArrayDeque',
        'ArrayList',
        ARRAY['OOP', 'COLLECTIONS']
    ),
    (
        'B05',
        'BEGINNER',
        'O que caracteriza uma interface funcional em Java?',
        'Uma interface funcional possui exatamente um metodo abstrato e pode ser alvo de lambda.',
        2,
        'Possuir somente metodos static',
        'Possuir exatamente um metodo abstrato',
        'Estender obrigatoriamente Collection',
        'Nao permitir implementacoes',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'B06',
        'BEGINNER',
        'Qual recurso permite passar um comportamento curto para forEach?',
        'Uma expressao lambda implementa a interface funcional Consumer esperada por forEach.',
        1,
        'Expressao lambda',
        'Bloco static',
        'Classe sealed',
        'Anotacao Override',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'B07',
        'BEGINNER',
        'Qual operacao de Stream transforma cada elemento em outro valor?',
        'map aplica uma funcao a cada elemento e produz um novo stream com os resultados.',
        3,
        'filter',
        'count',
        'map',
        'distinct',
        ARRAY['OOP', 'STREAMS']
    ),
    (
        'B08',
        'BEGINNER',
        'Qual operacao de Stream mantem apenas elementos que satisfazem uma condicao?',
        'filter recebe um Predicate e mantem os elementos para os quais ele retorna true.',
        4,
        'sorted',
        'limit',
        'peek',
        'filter',
        ARRAY['OOP', 'STREAMS']
    ),
    (
        'B09',
        'BEGINNER',
        'Como uma Collection pode iniciar um pipeline sequencial de Stream?',
        'O metodo stream da Collection cria um stream sequencial sobre seus elementos.',
        1,
        'Chamando stream()',
        'Chamando clone()',
        'Chamando wait()',
        'Chamando hashCode()',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'B10',
        'BEGINNER',
        'Qual operacao remove elementos duplicados de um Stream?',
        'distinct usa a igualdade dos objetos para manter apenas elementos distintos.',
        2,
        'flatMap',
        'distinct',
        'reduce',
        'skip',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'B11',
        'BEGINNER',
        'Qual operacao terminal pode reunir elementos de um Stream em uma List?',
        'collect com Collectors.toList acumula os elementos em uma lista.',
        3,
        'map(Collectors.toList())',
        'filter(Collectors.toList())',
        'collect(Collectors.toList())',
        'peek(Collectors.toList())',
        ARRAY['COLLECTIONS', 'STREAMS']
    ),
    (
        'B12',
        'BEGINNER',
        'O que acontece com a Collection original ao executar filter em seu Stream?',
        'Operacoes de stream produzem um pipeline e nao alteram automaticamente a fonte.',
        4,
        'Todos os elementos rejeitados sao removidos',
        'A Collection se torna imutavel',
        'A Collection e convertida em Set',
        'A Collection nao e alterada automaticamente',
        ARRAY['COLLECTIONS', 'STREAMS']
    ),
    (
        'I01',
        'INTERMEDIATE',
        'Qual regra deve ser respeitada ao sobrescrever equals em objetos usados como chave?',
        'Objetos iguais por equals precisam produzir o mesmo hashCode para colecoes baseadas em hash.',
        2,
        'equals deve sempre retornar false',
        'Objetos iguais devem ter o mesmo hashCode',
        'hashCode deve ser aleatorio',
        'equals deve comparar somente referencias',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'I02',
        'INTERMEDIATE',
        'Qual problema ocorre ao alterar um campo usado em hashCode depois de inserir a chave em HashMap?',
        'A chave pode passar a apontar para outro bucket logico e deixar de ser encontrada.',
        3,
        'O mapa e convertido em TreeMap',
        'Todos os valores sao removidos',
        'A chave pode nao ser localizada novamente',
        'O compilador impede a alteracao',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'I03',
        'INTERMEDIATE',
        'Quando a composicao costuma ser preferivel a heranca?',
        'Composicao reduz acoplamento quando a relacao representa tem-um e permite trocar colaboradores.',
        1,
        'Quando o objeto usa outro objeto sem representar um subtipo',
        'Quando toda classe precisa ser final',
        'Quando metodos privados devem ser sobrescritos',
        'Quando nao existem interfaces',
        ARRAY['OOP', 'COLLECTIONS']
    ),
    (
        'I04',
        'INTERMEDIATE',
        'O que significa usar List<? extends Number>?',
        'A lista produz valores Number, mas o tipo concreto e desconhecido e impede insercoes comuns.',
        4,
        'A lista aceita qualquer Object',
        'A lista aceita inserir qualquer Number',
        'A lista contem somente Integer',
        'A lista produz Number de um subtipo desconhecido',
        ARRAY['OOP', 'COLLECTIONS']
    ),
    (
        'I05',
        'INTERMEDIATE',
        'Por que map e uma operacao intermediaria lazy em Stream?',
        'Sua funcao so e executada quando uma operacao terminal consome o pipeline.',
        2,
        'Porque sempre executa em outra thread',
        'Porque aguarda uma operacao terminal',
        'Porque grava resultados em cache',
        'Porque modifica a Collection de origem',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'I06',
        'INTERMEDIATE',
        'Qual vantagem uma referencia de metodo pode oferecer em um pipeline?',
        'Ela reutiliza um metodo compativel com a interface funcional e pode deixar a intencao mais clara.',
        3,
        'Ignorar o sistema de tipos',
        'Executar sempre em paralelo',
        'Reutilizar um metodo compativel com a interface funcional',
        'Transformar qualquer metodo em static',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'I07',
        'INTERMEDIATE',
        'Qual diferenca principal existe entre map e flatMap?',
        'flatMap transforma cada elemento em um stream e achata os streams resultantes.',
        1,
        'flatMap achata os streams produzidos pela funcao',
        'map aceita somente valores numericos',
        'flatMap e sempre uma operacao terminal',
        'map remove valores duplicados',
        ARRAY['OOP', 'STREAMS']
    ),
    (
        'I08',
        'INTERMEDIATE',
        'Por que orElseGet pode ser preferivel a orElse com um valor caro?',
        'orElseGet calcula o valor alternativo somente quando o Optional esta vazio.',
        4,
        'Porque ignora o valor presente',
        'Porque transforma Optional em Stream automaticamente',
        'Porque aceita somente constantes',
        'Porque cria o valor alternativo sob demanda',
        ARRAY['OOP', 'STREAMS']
    ),
    (
        'I09',
        'INTERMEDIATE',
        'Qual collector agrupa elementos por uma chave calculada?',
        'Collectors.groupingBy recebe uma funcao classificadora e cria grupos por chave.',
        3,
        'Collectors.joining',
        'Collectors.counting',
        'Collectors.groupingBy',
        'Collectors.averagingInt',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'I10',
        'INTERMEDIATE',
        'Como evitar ConcurrentModificationException ao remover itens durante uma iteracao?',
        'Iterator.remove remove com seguranca o elemento atual da iteracao quando suportado.',
        2,
        'Alterar a lista dentro de forEach',
        'Usar Iterator.remove',
        'Chamar System.gc',
        'Converter todos os elementos em null',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'I11',
        'INTERMEDIATE',
        'Qual estrutura preserva a ordem de insercao de chaves em um Map?',
        'LinkedHashMap mantem uma lista ligada entre entradas e preserva a ordem configurada.',
        1,
        'LinkedHashMap',
        'HashMap',
        'WeakHashMap',
        'ConcurrentHashMap',
        ARRAY['COLLECTIONS', 'STREAMS']
    ),
    (
        'I12',
        'INTERMEDIATE',
        'Qual cuidado e necessario ao usar sorted em um Stream de objetos?',
        'Os elementos precisam ser comparaveis ou a operacao deve receber um Comparator compativel.',
        4,
        'Os objetos precisam estender Thread',
        'O stream precisa ser paralelo',
        'A fonte precisa ser um HashSet',
        'Deve existir ordem natural ou Comparator',
        ARRAY['COLLECTIONS', 'STREAMS']
    ),
    (
        'A01',
        'ADVANCED',
        'Como o principio de substituicao de Liskov afeta uma hierarquia usada por algoritmos genericos?',
        'Subtipos devem preservar as expectativas do contrato para substituir o tipo base sem quebrar o algoritmo.',
        3,
        'Subtipos devem remover pre-condicoes do compilador',
        'Subtipos devem sempre adicionar estado mutavel',
        'Subtipos devem preservar o contrato observavel do tipo base',
        'Subtipos nao podem implementar interfaces',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'A02',
        'ADVANCED',
        'Segundo PECS, qual wildcard e adequado para uma lista que apenas recebe Integer?',
        'Um consumidor de Integer usa ? super Integer para aceitar Integer com seguranca.',
        2,
        '? extends Integer',
        '? super Integer',
        '? implements Integer',
        '? exact Integer',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'A03',
        'ADVANCED',
        'Qual risco existe ao usar uma chave mutavel em uma estrutura baseada em hash?',
        'Mudar campos de igualdade pode tornar a entrada inacessivel no bucket calculado originalmente.',
        4,
        'A estrutura passa a aceitar null automaticamente',
        'A chave e clonada pelo compilador',
        'A estrutura se torna ordenada',
        'A entrada pode deixar de ser encontrada',
        ARRAY['OOP', 'COLLECTIONS']
    ),
    (
        'A04',
        'ADVANCED',
        'Qual operacao de ConcurrentHashMap atualiza um valor de forma atomica por chave?',
        'compute executa a funcao de remapeamento de maneira atomica para a chave.',
        1,
        'compute',
        'iterator',
        'values',
        'forEachOrdered',
        ARRAY['OOP', 'COLLECTIONS']
    ),
    (
        'A05',
        'ADVANCED',
        'Qual requisito um Collector deve respeitar para funcionar corretamente em streams paralelos?',
        'Supplier, accumulator e combiner devem produzir um resultado equivalente independentemente da particao.',
        3,
        'Usar sempre uma lista global mutavel',
        'Ignorar a funcao combiner',
        'Combinar particoes preservando o contrato do collector',
        'Executar somente no common pool',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'A06',
        'ADVANCED',
        'Qual papel o Spliterator exerce na execucao paralela de streams?',
        'Ele percorre e pode particionar os elementos para processamento por tarefas diferentes.',
        2,
        'Persistir os elementos no banco',
        'Percorrer e dividir a fonte em particoes',
        'Impedir operacoes terminais',
        'Substituir interfaces funcionais',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'A07',
        'ADVANCED',
        'Por que sorted pode reduzir o ganho de um stream paralelo?',
        'sorted e stateful e precisa coordenar elementos para determinar a ordem global.',
        1,
        'Porque precisa considerar o conjunto para produzir uma ordem global',
        'Porque transforma todos os valores em String',
        'Porque desativa o garbage collector',
        'Porque remove automaticamente elementos repetidos',
        ARRAY['OOP', 'STREAMS']
    ),
    (
        'A08',
        'ADVANCED',
        'Quando forEachOrdered e relevante em um stream paralelo?',
        'Ele preserva a encounter order quando a ordem observavel precisa ser mantida.',
        4,
        'Quando nenhum elemento possui ordem',
        'Quando o resultado deve ser um Set',
        'Quando a operacao precisa ser lazy',
        'Quando a ordem de encontro deve ser preservada',
        ARRAY['OOP', 'STREAMS']
    ),
    (
        'A09',
        'ADVANCED',
        'Qual caracteristica de distinct influencia seu comportamento com objetos?',
        'distinct usa equals e hashCode para reconhecer elementos semanticamente iguais.',
        2,
        'Ele compara apenas enderecos de memoria',
        'Ele depende do contrato de equals e hashCode',
        'Ele exige que todo objeto seja Comparable',
        'Ele altera a Collection de origem',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'A10',
        'ADVANCED',
        'Por que reduzir em uma colecao mutavel compartilhada e perigoso em stream paralelo?',
        'Varias tarefas podem modificar o mesmo estado e causar condicoes de corrida ou resultados incorretos.',
        3,
        'Porque reduce aceita somente tipos primitivos',
        'Porque streams paralelos nao aceitam lambdas',
        'Porque mutacao compartilhada pode causar condicoes de corrida',
        'Porque a JVM converte a colecao em array',
        ARRAY['OOP', 'COLLECTIONS', 'STREAMS']
    ),
    (
        'A11',
        'ADVANCED',
        'Como toMap deve tratar duas entradas que produzem a mesma chave?',
        'Sem uma funcao de merge, chaves duplicadas causam excecao; um merge define como combinar valores.',
        1,
        'Fornecendo uma funcao de merge quando duplicatas sao possiveis',
        'Convertendo a chave em indice numerico',
        'Usando sempre parallelStream',
        'Removendo equals da classe',
        ARRAY['COLLECTIONS', 'STREAMS']
    ),
    (
        'A12',
        'ADVANCED',
        'Qual propriedade deve ter a identidade usada em reduce?',
        'Ela deve ser neutra e associativa com o acumulador para qualquer particionamento.',
        4,
        'Ser sempre null',
        'Ser um objeto mutavel compartilhado',
        'Depender da ordem de threads',
        'Ser elemento neutro para a operacao',
        ARRAY['COLLECTIONS', 'STREAMS']
    );

INSERT INTO questions (
    id,
    statement,
    difficulty,
    explanation,
    active,
    created_at,
    updated_at
)
SELECT
    md5('code-arena:question:' || seed_key)::uuid,
    statement,
    difficulty,
    explanation,
    TRUE,
    TIMESTAMPTZ '2026-07-30 00:00:00+00',
    TIMESTAMPTZ '2026-07-30 00:00:00+00'
FROM seed_questions;

INSERT INTO question_categories (question_id, category_id)
SELECT
    md5('code-arena:question:' || question.seed_key)::uuid,
    category.id
FROM seed_questions question
CROSS JOIN LATERAL unnest(question.category_slugs) AS selected_category(slug)
JOIN categories category ON category.slug = selected_category.slug;

INSERT INTO alternatives (
    id,
    question_id,
    text,
    correct,
    display_order
)
SELECT
    md5(
        'code-arena:alternative:'
        || question.seed_key
        || ':'
        || alternative.display_order
    )::uuid,
    md5('code-arena:question:' || question.seed_key)::uuid,
    alternative.text,
    alternative.display_order = question.correct_order,
    alternative.display_order
FROM seed_questions question
CROSS JOIN LATERAL (
    VALUES
        (1::SMALLINT, question.alternative_1),
        (2::SMALLINT, question.alternative_2),
        (3::SMALLINT, question.alternative_3),
        (4::SMALLINT, question.alternative_4)
) AS alternative(display_order, text);
