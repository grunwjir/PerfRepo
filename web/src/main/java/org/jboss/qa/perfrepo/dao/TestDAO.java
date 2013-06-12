package org.jboss.qa.perfrepo.dao;

import java.util.List;

import javax.inject.Named;

import org.jboss.qa.perfrepo.model.Test;

@Named
public class TestDAO extends DAO<Test, Long> {

   public Test findByUid(String uid) {
      List<Test> tests = findAllByProperty("uid", uid);
      if (tests.size() > 0)
         return tests.get(0);
      return null;
   }
   
}