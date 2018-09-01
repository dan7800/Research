#!/bin/bash


## Description: Just run a file and note the start time & end time


# input Location
inputLocation=/home/mperm/Desktop/allapks

logFile=log2.txt

# clear the log file
#rm -rf $logFile
#touch $logFile


function analyzeAPK {
	fileName=$1

	date1=$(date +"%s")
	echo "Start" $fileName `date` >> $logFile

	cd /home/mperm/Desktop/mperm/MPermission/

	python3 MPerm.py -d /home/mperm/Desktop/mperm/MPermission/sample_apks/$fileName

	cd /home/mperm/Desktop/timeanalysis

	date2=$(date +"%s")

	diff=$(($date2-$date1))
	echo $f "> $(($diff % 60)) seconds."
	echo "Done" `date`
	echo "End" $fileName `date` " Diff:" $diff >> $logFile
	echo $date1
	echo $date2
	echo $diff
	echo "*****************" >> $logFile

}



#analyzeAPK adobe.apk


FILES=$(find $inputLocation -maxdepth 1 -type f  -name '*.apk')
	for f in $FILES
	do
		#analyzeAPK $f
		#echo $f
		filename=$(basename "$f")
		echo $filename
		analyzeAPK $filename
	done



exit
# 

#Log File
logFile=log.txt


# clear the log file
rm -rf $logFile
touch $logFile




# input Location
inputLocation=/home/mperm/Desktop/allapks


	function analyzeAPK {

        # Get start time
        date1=$(date +"%s")

        # Decompile
			## MPerm

			# python3 MPerm.py -d '/home/mperm/Desktop/mperm/MPermission/sample_apks/stitcher.apk'


			#python3 MPermission/MPerm.py -d $1
#echo $1	
	#		python3 /home/mperm/Desktop/mperm/MPermission/MPerm.py -d $1

	#echo $f


	cd /home/mperm/Desktop/mperm/MPermission/
	python3 MPerm.py -d $1
	


        # Get Time when done decompiling
        date2=$(date +"%s") 

        # Write decompile time
        diff=$(($date2-$date1))
        echo $diff
        echo $f "> $(($diff % 60)) seconds."  >> $logFile


    }



	FILES=$(find $inputLocation -maxdepth 1 -type f  -name '*.apk')
	for f in $FILES
	do
		analyzeAPK $f
	done


