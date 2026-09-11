package ifrs.edu.avaliacao_mnr.dto;

/* T: I opted for a RECORD because I was re-reading some notes and saw that it is a special type of class used to represent immutable data. It is leaner and more abstract, ideal for classes that only carry data and have no complex logic. The constructor, getters and setters, equals, hashcode, and toString are generated automatically. */

/* JT: In PdfPageValidationService we put the PDF validation rules. For the video part, our colleague ALLAN will create a separate class to validate the video.
It was decided that if it falls outside the rule, IT WILL NOT AUTOMATICALLY DISQUALIFY. The system will warn the evaluator that the project did not meet the requirements and needs to be reviewed manually, and it will remain pending evaluation. */

/* JT: We imported the PDF and video validation classes so that CsvParserService can use the mathematical rules and the PDF reader we just created in PdfPageValidationService. */

public record ProjectImportDTO(
        // === DATA USED IN THIS MVP (ACTIVE) ===
        String projectName,       
        String pdfUrl,            
        String level,             
        String videoUrl,          
        String participantName,   
        String participantCpf,    
        String participantEmail,  
        String institutionName,   

        /* JT: For the system to apply this new logic, it will need to "remember" that the project failed automatic validation in order to notify the evaluator on the front-end screen later, and then the evaluator can review the project. */

        // JT: New fields for automatic screening control
        boolean markedForReview,
        boolean validated
) {} 

// === DATA NOT USED AT THE MOMENT ===
/*
String photoPath,                          
String onlyGirlsCategory,                  
String status,                             
String eventFullName,                      
String participantPassport,                
String participantPhoneNumber,             
String institutionClassificationName,      
String institutionShippingZipCode,         
String institutionShippingStateName,       
String institutionShippingCityName,        
String institutionShippingNeighborhood,    
String institutionShippingAddress,         
String institutionShippingNumber,          
String institutionShippingComplement,      
String institutionShippingAddressTypeName, 
String countryName,                        
String stateName,                          
String cityName                            
*/
