import os

java_dir = "../app/src/main/java/com/map/snap/snapmap"
xml_dir = "../app/src/main/res/layout"

# {javafile, xmlfile}
FileMap = {}

# {javafile, {{ElementType:ElementID},{ElementType:ElementID}}}
GUIMap = {}

GUIElements = {'EditText', 'Button'}

# Gets xml layout for Activity File
for file in os.listdir(java_dir):

    with open(java_dir + "/" + file) as f:
        lines = f.readlines()

    for line in lines:
        if 'setContentView(R.layout.' in line:
            #print line.strip().split('.')[2][:-2]
            FileMap[file] = line.strip().split('.')[2][:-2]

#print FileMap

print FileMap.values()
for file in FileMap.values():
    print file
    with open(xml_dir + "/" + file + ".xml") as f:
        lines = f.readlines()
    flag = False
    current = ''
    for line in lines:
        if any(word in line for word in GUIElements):
            flag = True
        if flag:
            if 'android:id="@+id/' in line:
                print line.strip().split('/')[1][:-1]
                try:
                    GUIMap[file] = line.strip().split('/')[1][:-1]
                    flag = False
                except KeyError:
                    GUIMap[file] = line.strip().split('/')[1][:-1]

            # get ID
            #GUIMap[file] = word
    print '\n\n'


print GUIMap



# get list of java Files
# get list of xml Files
# save in FileMap

# check if ui element in xml exists in java file
    # get list of ui elements in each xml file with id and save in GUIMap
    # compare GUIMap with elements in each java file and enter those which are missing (after setContentView)
