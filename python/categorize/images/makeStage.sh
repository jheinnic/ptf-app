#!/bin/sh

STAGE_DIR="/home/ionadmin/images"
ARCHIVE_FILES="/mnt/array/inspector/media/archive_files"
BEAD_DENSITY="CSA/outputs/SigProcActor-00/Bead_density_1000.png"

for ii in "00" "01" "02" "03" "04" "05" "06" "07" "08" "09" "10" "11" "12" "13" "14" "15" "16" "17" "18" "19" "20" "21" "22" "23" "24" "25" "26" "27" "28" "29" "30" "31" 
do
	mkdir -p ${STAGE_DIR}/${ii}
done

for archive_id in `cat ra-va.log | awk '{print $1}'`
do
	subdir=`echo "$archive_id % 32" | bc`
	SRC_FILE="${ARCHIVE_FILES}/${archive_id}/${BEAD_DENSITY}"
	if [ -f "${SRC_FILE}" ]
	then
		DST_FILE="${STAGE_DIR}/${subdir}/${archive_id}_BeadDensity_1000.png"
		sudo cp "${SRC_FILE}" "${DST_FILE}"
		echo ${DST_FILE} >> collected_log.dat
	fi
done
