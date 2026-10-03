package com.curseddomain.technique;

public final class PlaceholderTechnique extends Technique {
   public PlaceholderTechnique(Technique.Properties properties) {
      super(properties);
   }

   @Override
   public boolean isImplemented() {
      return false;
   }
}
