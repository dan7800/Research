rm(list = ls())
#setwd("Desktop/data/")

G1 = read.csv("data/0-3.csv", header = T)
G2 = read.csv("data/3-5.csv", header = T)
G3 = read.csv("data/all.csv", header = T)


############################## Spearman Analysis ###############################

### Spearman

print ("**************** Spearman **************")
### LOC Correlations
print("----- LOC ----- ")
 print(paste("OPriv:", "SP:", cor(G3$LOC,G3$OPrivCount,method="spearman"), "K:",cor(G3$LOC,G3$OPrivCount,method="kendall")))
 print(paste("UPriv:", "SP:",cor(G3$LOC,G3$UPrivCount,method="spearman"), "K:",cor(G3$LOC,G3$UPrivCount,method="kendall")))
 print(paste("PermCount:", "SP:",cor(G3$LOC,G3$PermissionCount,method="spearman"), "K:",cor(G3$LOC,G3$PermissionCount ,method="kendall")))
 print(paste("FuzzyRisk:", "SP:",cor(G3$LOC,G3$FuzzyRisk,method="spearman"), "K:",cor(G3$LOC,G3$FuzzyRisk,method="kendall")))
 print(paste("DefectCount:", "SP:",cor(G3$LOC,G3$DefectCount,method="spearman"), "K:",cor(G3$LOC,G3$DefectCount,method="kendall")))


print("----- Java Files ----- ")
### Java Files Correlations
 print(paste("OPriv:", "SP:",cor(G3$JavaFiles,G3$OPrivCount,method="spearman"), "K:",cor(G3$LOC,G3$OPrivCount,method="kendall")))
 print(paste("UPriv:", "SP:",cor(G3$JavaFiles,G3$UPrivCount,method="spearman"), "K:",cor(G3$LOC,G3$UPrivCount,method="kendall")))
 print(paste("PermCount:", "SP:",cor(G3$JavaFiles,G3$PermissionCount,method="spearman"), "K:",cor(G3$LOC,G3$PermissionCount,method="kendall")))
 print(paste("FuzzyRisk:", "SP:",cor(G3$JavaFiles,G3$FuzzyRisk,method="spearman"), "K:",cor(G3$LOC,G3$FuzzyRisk,method="kendall")))

print("----- PrivCount ----- ")
### PrivCount Correlations
 print(paste("OPriv:", "SP:",cor(G3$PermissionCount,G3$OPrivCount,method="spearman"), "K:",cor(G3$LOC,G3$OPrivCount,method="kendall")))
 print(paste("UPriv:", "SP:",cor(G3$PermissionCount,G3$UPrivCount,method="spearman"), "K:",cor(G3$LOC,G3$UPrivCount,method="kendall")))
 print(paste("FuzzyRisk:", "SP:",cor(G3$PermissionCount,G3$FuzzyRisk,method="spearman"), "K:",cor(G3$LOC,G3$FuzzyRisk,method="kendall")))


### Correlations for 0-3, 3-5


### Do a box plot
### This is not working yet
# boxplot(G3[,i], G3[,i])




### Kendell

#print ("**************** kendall **************")
### LOC Correlations
#print("----- LOC ----- ")
# print(paste("OPriv:", cor(G3$LOC,G3$OPrivCount,method="kendall")))
# print(paste("UPriv:", cor(G3$LOC,G3$UPrivCount,method="kendall")))
# print(paste("PermCount:", cor(G3$LOC,G3$PermissionCount,method="kendall")))
# print(paste("FuzzyRisk:", cor(G3$LOC,G3$FuzzyRisk,method="kendall")))


#print("----- Java Files ----- ")
### Java Files Correlations
# print(paste("OPriv:", cor(G3$JavaFiles,G3$OPrivCount,method="kendall")))
# print(paste("UPriv:", cor(G3$JavaFiles,G3$UPrivCount,method="kendall")))
# print(paste("PermCount:", cor(G3$JavaFiles,G3$PermissionCount,method="kendall")))
# print(paste("FuzzyRisk:", cor(G3$JavaFiles,G3$FuzzyRisk,method="kendall")))

#print("----- PrivCount ----- ")
### PrivCount Correlations
# print(paste("OPriv:", cor(G3$PermissionCount,G3$OPrivCount,method="kendall")))
# print(paste("UPriv:", cor(G3$PermissionCount,G3$UPrivCount,method="kendall")))
# print(paste("FuzzyRisk:", cor(G3$PermissionCount,G3$FuzzyRisk,method="kendall")))
