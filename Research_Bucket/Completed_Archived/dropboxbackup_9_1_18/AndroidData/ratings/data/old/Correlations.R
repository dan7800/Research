rm(list = ls())

### Remember to change this below
#setwd("/Users/dxkvse/Desktop/Rdata/")

## source('Correlations.R')

#G1 = read.csv("0-3_No_LOC.csv", header = T)
#G2 = read.csv("3-5_No_LOC.csv", header = T)

G1 = read.csv("0-3-Tools_No_LOC.csv", header = T)
G2 = read.csv("3-5-Tools_No_LOC.csv", header = T)

#G1 = read.csv("0-3-Entertainment_No_LOC.csv", header = T)
#G2 = read.csv("3-5-Entertainment_No_LOC.csv", header = T)


#G1 = read.csv("0-3-Education_No_LOC.csv", header = T)
#G2 = read.csv("3-5-Education_No_LOC.csv", header = T)






#G1 = read.csv("0-3-Business.csv", header = T)
#G2 = read.csv("3-5-Business.csv", header = T)



#G1 = read.csv("0-3-Lifestyle.csv", header = T)
#G2 = read.csv("3-5-Lifestyle.csv", header = T)

#G1 = read.csv("0-3-Books.csv", header = T)
#G2 = read.csv("3-5-Books.csv", header = T)

#G1 = read.csv("0-3-Personalization.csv", header = T)
#G2 = read.csv("3-5-Personalization.csv", header = T)

#G1 = read.csv("0-3-Puzzle_No_LOC.csv", header = T)
#G2 = read.csv("3-5-Puzzle_No_LOC.csv", header = T)


#G1 = read.csv("0-3-Communication.csv", header = T)
#G2 = read.csv("3-5-Communication.csv", header = T)

#G1 = read.csv("0-3-Music.csv", header = T)
#G2 = read.csv("3-5-Music.csv", header = T)



##### Things should be being divivded by Row 8 (LOC) to normalize the results


i = 3
n = ncol(G1)
name_vec = names(G1)

G1[,5:7] = 1000*(G1[,5:7]/G1[,8])
G1[,9:n] = 1000*(G1[,9:n]/G1[,8])
G2[,5:7] = 1000*(G2[,5:7]/G2[,8])
G2[,9:n] = 1000*(G2[,9:n]/G2[,8]) 




while(i <= n)
{
  l = wilcox.test(G1[,i], G2[,i], alternative="less")


  ### Just show all

    g = wilcox.test(G1[,i], G2[,i], alternative="greater")
    print(paste(name_vec[i], "is less with p value", l$p.value))
    print(paste(name_vec[i], "is greater with p value", g$p.value))

## ^^^^^ Just show all



 # if(l$p.value < 0.05)
 #   print(paste(name_vec[i], "is less with p value", l$p.value))
 # g = wilcox.test(G1[,i], G2[,i], alternative="greater")
 # if(g$p.value < 0.05)
 #   print(paste(name_vec[i], "is greater with p value", g$p.value))
 # if(l$p.value < 0.05 | g$p.value < 0.05)
 # {
	### Take out the boxplot for now
   # boxplot(G1[,i], G2[,i])
 #   print(paste(name_vec[i], "0 - 3 : ", median(G1[,i])))
 #   print(paste(name_vec[i], "3 - 5 : ", median(G2[,i])))  
 # }
  i = i + 1
}

#setwd("/Users/dxkvse/Desktop/")
