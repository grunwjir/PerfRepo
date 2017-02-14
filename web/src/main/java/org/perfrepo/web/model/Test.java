/**
 * PerfRepo
 * <p>
 * Copyright (C) 2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.perfrepo.web.model;

import org.perfrepo.web.model.user.Group;
import org.perfrepo.web.model.user.User;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents one test.
 *
 * @author Pavel Drozd (pdrozd@redhat.com)
 * @author Michal Linhard (mlinhard@redhat.com)
 */
@javax.persistence.Entity
@Table(name = "test")
@NamedQueries({
    @NamedQuery(name = Test.FIND_BY_UID, query = "SELECT test FROM Test test WHERE test.uid = :uid")
})
public class Test implements Entity<Test> {

   private static final long serialVersionUID = 2936849220074718535L;

   public static final String FIND_BY_UID = "Test.findByUid";

   @Id
   @SequenceGenerator(name = "TEST_ID_GENERATOR", sequenceName = "TEST_SEQUENCE", allocationSize = 1)
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEST_ID_GENERATOR")
   private Long id;

   @Column(name = "name")
   @NotNull(message = "{test.nameRequired}")
   @Size(max = 2047)
   private String name;

   @ManyToMany(fetch = FetchType.LAZY)
   @JoinTable(
           name = "test_metric",
           joinColumns = {@JoinColumn(name = "test_id", nullable = false, updatable = false)},
           inverseJoinColumns = {@JoinColumn(name = "metric_id", nullable = false, updatable = false)}
   )
   private Set<Metric> metrics = new TreeSet<>();

   @ManyToMany(fetch = FetchType.LAZY)
   @JoinTable(
       name = "test_subscriber",
       joinColumns = {@JoinColumn(name = "test_id", nullable = false, updatable = false)},
       inverseJoinColumns = {@JoinColumn(name = "user_id", nullable = false, updatable = false)}
   )
   private Set<User> subscribers = new HashSet<>();

   @Column(name = "uid", unique = true)
   @NotNull(message = "{test.uidRequired}")
   @Size(max = 2047)
   private String uid;

   @ManyToOne(fetch = FetchType.EAGER)
   @NotNull(message = "{test.groupRequired}")
   @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false)
   private Group group;

   @Column(name = "description")
   @Size(max = 10239)
   private String description;

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public Set<User> getSubscribers() {
      return subscribers;
   }

   public Set<Metric> getMetrics() {
      return metrics;
   }

   public void setUid(String uid) {
      this.uid = uid;
   }

   public String getUid() {
      return this.uid;
   }

   public Group getGroup() {
      return group;
   }

   public void setGroup(Group group) {
      this.group = group;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public void setMetrics(Set<Metric> metrics) {
      this.metrics = metrics;
   }

   public void setSubscribers(Set<User> subscribers) {
      this.subscribers = subscribers;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Test)) return false;

      Test test = (Test) o;

      return getUid() != null ? getUid().equals(test.getUid()) : test.getUid() == null;
   }

   @Override
   public int hashCode() {
      return getUid() != null ? getUid().hashCode() : 0;
   }

}