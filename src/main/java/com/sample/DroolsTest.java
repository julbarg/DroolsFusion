package com.sample;

import java.util.Collection;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;

import com.claro.cpymes.dto.LogDTO;


/** This is a sample class to launch a rule. */
public class DroolsTest {

   public static final void main(String[] args) {
      try {
         // load up the knowledge base
         KnowledgeBase kbase = readKnowledgeBase();
         StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
         KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");

         WorkingMemoryEntryPoint logFiltrado = ksession.getWorkingMemoryEntryPoint("logs-filtrado");

         LogDTO log = new LogDTO();

         log.setNodo("FLORA");
         log.setNameCorrelation("Afectacion Masiva");
         log.setNameEvent("GponOntConfigRecoveryFailAlarm");

         ksession.fireAllRules();

         logFiltrado.insert(log);
         ksession.fireAllRules();

         WorkingMemoryEntryPoint logCorrelation = ksession.getWorkingMemoryEntryPoint("logs-correlation");

         log = new LogDTO();
         log.setNodo("FLORA");
         log.setIp("172.23.2.2");
         log.setNameCorrelation("Afectacion Masiva");
         log.setNameEvent("linkDown");
         logCorrelation.insert(log);
         ksession.fireAllRules();

         Collection<Object> collect = logCorrelation.getObjects();
         for (Object l : collect) {
            if (l instanceof LogDTO) {
               LogDTO o = (LogDTO) l;
               System.out.println(o);
            }
         }

         collect = ksession.getObjects();
         for (Object l : collect) {
            if (l instanceof LogDTO) {
               LogDTO o = (LogDTO) l;
               System.out.println("ksession: " + o);
            }
         }

         

         logger.close();
      } catch (Throwable t) {
         t.printStackTrace();
      }
   }

   private static KnowledgeBase readKnowledgeBase() throws Exception {
      KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
      config.setOption(EventProcessingOption.STREAM);

      KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
      kbuilder.add(ResourceFactory.newClassPathResource("Fusion.drl"), ResourceType.DRL);
      KnowledgeBuilderErrors errors = kbuilder.getErrors();
      if (errors.size() > 0) {
         for (KnowledgeBuilderError error : errors) {
            System.err.println(error);
         }
         throw new IllegalArgumentException("Could not parse knowledge.");
      }
      KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(config);
      kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
      return kbase;
   }

   public static class Message {

      public static final int HELLO = 0;

      public static final int GOODBYE = 1;

      private String message;

      private int status;

      public String getMessage() {
         return this.message;
      }

      public void setMessage(String message) {
         this.message = message;
      }

      public int getStatus() {
         return this.status;
      }

      public void setStatus(int status) {
         this.status = status;
      }

   }

}
