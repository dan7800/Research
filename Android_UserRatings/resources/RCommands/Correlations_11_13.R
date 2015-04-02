rm(list = ls())
setwd("Desktop/data/")

G1 = read.csv("0-3.csv", header = T)
G2 = read.csv("3-5.csv", header = T)

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
  if(l$p.value < 0.05)
    print(paste(name_vec[i], "is less with p value", l$p.value))
  g = wilcox.test(G1[,i], G2[,i], alternative="greater")
  if(g$p.value < 0.05)
    print(paste(name_vec[i], "is greater with p value", g$p.value))
  if(l$p.value < 0.05 | g$p.value < 0.05)
    boxplot(G1[,i], G2[,i])
  i = i + 1
}
