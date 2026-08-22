package ifrs.edu.avaliacao_mnr.project.dto;

/* T: Optei por RECORD, porque estava relendo algumas anotações
e vi que é um tipo especial de classe, usada para representar dados imutáveis.
Fica mais enxuto e abstraído, ideal para classes que só carregam dados, mas
não tem lógica complexa. O construtor, getters e setters, equals, hashcode e
toString são gerados automaticamente.
*/

public record ProjectImportDTO(
        // 🟢 === DADOS UTILIZADOS NESTE MVP (ATIVOS) ===
        String projectName,       // Coluna: name
        String pdfUrl,            // Coluna: resumo__artigo_pdf_obrigatório
        String level,             // Coluna: qual_é_o_nível_do_trabalho_inscrito_obrigatório
        String videoUrl,          // Coluna: endereço_youtube_link_para_o_vídeo_de_apresentação_obrigatório
        String participantName,   // Coluna: person.name
        String participantCpf,    // Coluna: person.cpf (Atuará como registrationNumber)
        String participantEmail,  // Coluna: person.email
        String institutionName    // Coluna: institution.name (O Taylan já havia mapeado e é bom ter)

        // 🔴 === DADOS NÃO UTILIZADOS NO MOMENTO  ===
        /*
        String photoPath,                          // Coluna: photo_path
        String onlyGirlsCategory,                  // Coluna: o_trabalho_tem_como_autoras_apenas_...
        String status,                             // Coluna: status
        String eventFullName,                      // Coluna: event.full_name
        String participantPassport,                // Coluna: person.passport
        String participantPhoneNumber,             // Coluna: person.phone_number
        String institutionClassificationName,      // Coluna: institution.classification.name
        String institutionShippingZipCode,         // Coluna: institution.shipping_zip_code
        String institutionShippingStateName,       // Coluna: institution.shippingState.name
        String institutionShippingCityName,        // Coluna: institution.shippingCity.name
        String institutionShippingNeighborhood,    // Coluna: institution.shipping_neighborhood
        String institutionShippingAddress,         // Coluna: institution.shipping_address
        String institutionShippingNumber,          // Coluna: institution.shipping_number
        String institutionShippingComplement,      // Coluna: institution.shipping_complement
        String institutionShippingAddressTypeName, // Coluna: institution.shippingAddressType.name
        String countryName,                        // Coluna: country.name
        String stateName,                          // Coluna: state.name
        String cityName                            // Coluna: city.name
        */
) {}

/*JT: aqui vamos ter que colocar uma automação para quando tiver menos paginas 
que o mínimo no pdf e tempo de video menor que o mínimo, ele não deixa importar,
 ou seja, não deixa salvar no banco.
Acho que podemos fazer isso no service, mas não sei se é o melhor lugar.

1. Triagem Inicial (Critérios Eliminatórios) Verifique se o trabalho 
cumpriu as exigências do edital antes de iniciar a avaliação de notas: 
Resumo/Artigo: Enviado, segue o layout da MNR, dentro do limite de 
páginas e possui QR Code visível. Vídeo: Enviado, duração entre 3 a 
5 min, gravado na horizontal, legendado, com apresentação de estudante
 e layer inicial da MNR.*/