package ifrs.edu.avaliacao_mnr.dto;

/* T: I opted for a RECORD because I was re-reading some notes and saw that it is a special type of class used to represent immutable data. It is leaner and more abstract, ideal for classes that only carry data and have no complex logic. The constructor, getters and setters, equals, hashcode, and toString are generated automatically. */

/* JT: In PdfPageValidationService we put the PDF validation rules. For the video part, our colleague ALLAN will create a separate class to validate the video.
It was decided that if it falls outside the rule, IT WILL NOT AUTOMATICALLY DISQUALIFY. The system will warn the evaluator that the project did not meet the requirements and needs to be reviewed manually, and it will remain pending evaluation. */

/* JT: We imported the PDF and video validation classes so that CsvParserService can use the mathematical rules and the PDF reader we just created in PdfPageValidationService. */

public record ProjectImportDTO(
        // === DATA USED IN THIS MVP (ACTIVE) ===
        String projectName,       // Column: name
        String pdfUrl,            // Column: resumo__artigo_pdf_mandatory
        String level,             // Column: level_of_the_submitted_work_mandatory
        String videoUrl,          // Column: youtube_address_link_to_the_presentation_video_mandatory
        String participantName,   // Column: person.name
        String participantCpf,    // Column: person.cpf (Will act as registrationNumber)
        String participantEmail,  // Column: person.email
        String institutionName,   // Column: institution.name (Taylan had already mapped this and it's good to have)

        /* JT: For the system to apply this new logic, it will need to "remember" that the project failed automatic validation in order to notify the evaluator on the front-end screen later, and then the evaluator can review the project. */

        // JT: New fields for automatic screening control
        boolean markedForReview,
        boolean validated
) {} 

// === DATA NOT USED AT THE MOMENT ===
/*
String photoPath,                          // Column: photo_path
String onlyGirlsCategory,                  // Column: o_trabalho_tem_como_autoras_apenas_...
String status,                             // Column: status
String eventFullName,                      // Column: event.full_name
String participantPassport,                // Column: person.passport
String participantPhoneNumber,             // Column: person.phone_number
String institutionClassificationName,      // Column: institution.classification.name
String institutionShippingZipCode,         // Column: institution.shipping_zip_code
String institutionShippingStateName,       // Column: institution.shippingState.name
String institutionShippingCityName,        // Column: institution.shippingCity.name
String institutionShippingNeighborhood,    // Column: institution.shipping_neighborhood
String institutionShippingAddress,         // Column: institution.shipping_address
String institutionShippingNumber,          // Column: institution.shipping_number
String institutionShippingComplement,      // Column: institution.shipping_complement
String institutionShippingAddressTypeName, // Column: institution.shippingAddressType.name
String countryName,                        // Column: country.name
String stateName,                          // Column: state.name
String cityName                            // Column: city.name
*/