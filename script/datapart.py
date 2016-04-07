import csv
import datetime

def removeDup(rf, wf):
    data = []
    vids = []
    alist = {}
    mlist = {}

    with open(rf, 'r') as csvfile:
        reader = csv.DictReader(csvfile, delimiter=',')
        for temp_res in reader:
            d = datetime.datetime.strptime(temp_res['tpep_pickup_datetime'], "%Y-%m-%d %H:%M:%S")

            dstring = "%s-%s-%s" % (d.year, d.month, d.day)
            if d.day != 2:
                continue

            if d.hour < 12:
                dlist = mlist
            else:
                dlist = alist

            if (not dstring in dlist.keys()):
                dlist[dstring] = [temp_res]
            else:
                dlist[dstring].append(temp_res)

    for x in alist:
    	if len(alist[x]) != 0:

            outputfile = "afternoon_" + dstring + ".csv"

            with open(outputfile, 'w', encoding='utf8',newline='') as output_file:
                keys = alist[x][0].keys()
                dict_writer = csv.DictWriter(output_file, keys, delimiter=",")
                dict_writer.writeheader()
                dict_writer.writerows(alist[x])
    

    for x in mlist:
        if len(mlist[x]) != 0:

            outputfile = "morning_" + dstring + ".csv"

            with open(outputfile, 'w', encoding='utf8',newline='') as output_file:
                keys = mlist[x][0].keys()
                dict_writer = csv.DictWriter(output_file, keys, delimiter=",")
                dict_writer.writeheader()
                dict_writer.writerows(mlist[x])


    return True

removeDup("yellow_tripdata_2015-01.csv", "")
