package com.curseddomain.technique.impl;

import com.curseddomain.technique.Technique;

public abstract class ImplementedTechnique extends Technique {
   protected ImplementedTechnique(Technique.Properties properties) {
      super(properties);
   }

   @Override
   public boolean isImplemented() {
      return true;
   }
}
