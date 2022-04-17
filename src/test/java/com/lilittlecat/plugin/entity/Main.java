package com.lilittlecat.plugin.entity;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author LiLittleCat
 * @since 2022/4/17
 */
public class Main {

    static class Father {
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    static class Son {
        private String testString;
        private Integer testInteger;
        private BigDecimal testBigDecimal;
        private Father testObject;
        private List<String> testList;
        private Set<Float> testSet;
        private Map<String, BigDecimal> testMap;
        private Map<String, BigDecimal>[] testMapArray;
        private Father[] testObjectArray;
        private int testInt;
        private double[] testDoubleArray;

        public String getTestString() {
            return testString;
        }

        public void setTestString(String testString) {
            this.testString = testString;
        }

        public Integer getTestInteger() {
            return testInteger;
        }

        public void setTestInteger(Integer testInteger) {
            this.testInteger = testInteger;
        }

        public BigDecimal getTestBigDecimal() {
            return testBigDecimal;
        }

        public void setTestBigDecimal(BigDecimal testBigDecimal) {
            this.testBigDecimal = testBigDecimal;
        }

        public Father getTestObject() {
            return testObject;
        }

        public void setTestObject(Father testObject) {
            this.testObject = testObject;
        }

        public List<String> getTestList() {
            return testList;
        }

        public void setTestList(List<String> testList) {
            this.testList = testList;
        }

        public Set<Float> getTestSet() {
            return testSet;
        }

        public void setTestSet(Set<Float> testSet) {
            this.testSet = testSet;
        }

        public Map<String, BigDecimal> getTestMap() {
            return testMap;
        }

        public void setTestMap(Map<String, BigDecimal> testMap) {
            this.testMap = testMap;
        }

        public Map<String, BigDecimal>[] getTestMapArray() {
            return testMapArray;
        }

        public void setTestMapArray(Map<String, BigDecimal>[] testMapArray) {
            this.testMapArray = testMapArray;
        }

        public Father[] getTestObjectArray() {
            return testObjectArray;
        }

        public void setTestObjectArray(Father[] testObjectArray) {
            this.testObjectArray = testObjectArray;
        }

        public int getTestInt() {
            return testInt;
        }

        public void setTestInt(int testInt) {
            this.testInt = testInt;
        }

        public double[] getTestDoubleArray() {
            return testDoubleArray;
        }

        public void setTestDoubleArray(double[] testDoubleArray) {
            this.testDoubleArray = testDoubleArray;
        }
    }

    public void main(String[] args) {
        Son son = new Son();
    }
}
