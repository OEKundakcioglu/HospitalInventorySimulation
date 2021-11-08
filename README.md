# HospitalInventorySimulation
Simulation of hospital demand, inventory sharing, with reported service levels as KPIs


ejml-v0.40-libs: should be added to the classpath

Launcher: Main method is in this class

ProblemParameters: All user defined parameters are in this class

ProblemData: Defines the problem data

PerformAllSimulations: Performs all simulations and stores results

Simulate: Performs each simulation

ComputeOptimal: Static methods in this class compute optimal quantities using Efficient Java Matrix Library <http://ejml.org> for matrix operations

ComputeLinear: Static methods in this class compute quantities linearly proportional to the demand

Hospital: Stores hospital demand and id information

KPIs: Stores KPIs, demand, and inventory levels that are later reported in a csv file

Scenario: Stores the problem input and associated KPIs

StockType: enum for types of stock - pooled and safety stock
