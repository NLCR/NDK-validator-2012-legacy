package com.logica.ndk.tm.validation.validator.core;


public enum ValidationTypes {

   STRUCTURE_VALIDATION("Structure validation"),
   CROSS_VALIDATION("Cross validation"),
   MAIN_METS_USING_MESTXSD("Main mest validation using mets.xsd"),
   MAIN_METS_USING_MODSXSD("Main mest validation using mods.xsd"),
   MAIN_METS_FOR_PROFILE("Main mets validation using template: "),
   AMD_METS_FOR_PROFILE("ADM mets {fileName} validation using template: "),
   AMD_METS_USING_XSD("ADM mets {fileName} validation {type} using xsd: "),
   ALTO_VALIDATION("ALTO file: {fileName} validation using also.xsd");
   private String message;
   // $FF: synthetic field
   private static final ValidationTypes[] $VALUES = new ValidationTypes[]{STRUCTURE_VALIDATION, CROSS_VALIDATION, MAIN_METS_USING_MESTXSD, MAIN_METS_USING_MODSXSD, MAIN_METS_FOR_PROFILE, AMD_METS_FOR_PROFILE, AMD_METS_USING_XSD, ALTO_VALIDATION};


   private ValidationTypes(String message) {
      this.message = message;
   }

   public String getMessage() {
      return this.message;
   }

}
