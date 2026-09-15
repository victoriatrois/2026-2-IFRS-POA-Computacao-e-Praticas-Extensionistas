package ifrs.edu.avaliacao_mnr.dto;

public record ProjectImportDTO(
        String projectName,
        String pdfUrl,
        String level,
        String videoUrl,
        String participantName,
        String participantCpf,
        String participantEmail,
        String institutionName,
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
