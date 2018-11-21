import re
import rdfTest
# This yet another great python script maps BMS 
# raw data (in xml format) into seperated owl files 

# Magical regex <VAV_+[a-zA-Z]+[0-9]_[a-zA-Z0-9]*_WFH-.*>	
# WFH...Door
# WFH...status


# this function uses regular expressions to separate values from tags
# and seperate values and units 
def seperateContent(content,idRegex,filenameTemplate,IRITemplate,isSI,nameOfRow):
	valueRegex = '>.*<'
	tagRegex = '<[a-zA-Z0-9_ -]*>'
	# all the values are wrapped following such pattern : '<tagName>........</tagName>'
	# therefore, such regex identify the value wrapped within tags 
	numericalValueRegex = '-*[0-9]+.*[0-9]*.*' 
	# such regex will identify strings like '10.11','-0','100','10.11 cm','-0 %', which are numerical values
	
	value = re.search(valueRegex,content) # search out the value part
	if(value):		
		tag = (re.search(tagRegex,content).group(0))
		if(abbrPair):						# whether the case needs to get the abbriation from the dictionary
			#id  = abbrPairs[idRegex]
			#print(tag)
			#print(currentRoomName)
			#print(heaterAirflowIdTag)
			IRI = abbrPairs[currentRoomName]
			#print('IRI',IRI)
			
			#print('nameOfRow',nameOfRow)
			
			
			if(idRegex == 'roomItem'):
				id 	= IRI.replace('_','-')
			else:
				id	= (re.search(idRegex,tag)).group(0).replace('_','-')


					
			#IRI 	= 
			print('------ Room Name ------',currentRoomName)
			filename = (filenameTemplate %id)
			targetIRI = (IRITemplate %IRI)
			print('------- IRI -----------', targetIRI)
			
		else:
		
			if(type == '1'): 
		
				print('tag--- ',tag)
				if(re.match('.*AV_[a-zA-Z][0-9]_[0-9]*.*',tag)):
					id	= (re.search(idRegex,tag)).group(0).replace('_','-')
					filename = (filenameTemplate %id.replace('_','-'))
					targetIRI = (IRITemplate %(id[:5] + '/' + id[5:]).replace('_','-'))	

					
					# if(re.match('.*S[0-9]_[0-9]*',tag)):
						# targetIRI = targetIRI.replace('AirFlow','Airflow')
						# print('here')
						# print(filename)
						# print('targetIRI',targetIRI)
				else:
					id = heaterAirflowIdTag[:5] + '/' + heaterAirflowIdTag[5:]
					print('id',id)
					filename = (filenameTemplate %heaterAirflowIdTag.replace('_','-'))
					targetIRI = (IRITemplate %id.replace('_','-'))	
			
			elif(type == '2'):	
			
			
				print(re.match('CH-.*_CSStat_sensor1.owl',filenameTemplate))
				value = value.group(0).strip()[1:-1].strip()
				splittedArray = value.split(' ')			
				value = splittedArray[0]
				
				if(re.match('CH-.*_CSStat_sensor1.owl',filenameTemplate)):	
					id = (re.search(idRegex,tag)).group(0)
					id = id.replace('0','').replace('_','-')[1:]
					filename = (filenameTemplate%id)
					targetIRI = (IRITemplate %id)
					print('filename',filename)
					print('targetIRI',targetIRI)
					
					print('tag ---', tag.split(' ')[1][:-1])
						
					value = {'value':'Low' , 'type': 'string', 'unit': {'isSI': isSI, 'unit': ''}}
					
					
					rdfTest.appendOwlFile(filename,targetIRI,value,True)		
					return 0
				else:	

					if(value == '1'):
						print('gotcha')
						id = (re.search(idRegex,tag)).group(0)
						id = id.replace('0','').replace('_','-')[1:]
						filename = (filenameTemplate%id)
						targetIRI = (IRITemplate %id)
						print('filename',filename)
						print('targetIRI',targetIRI)
						
						print('tag ---', tag.split(' ')[1][:-1])
							
						value = {'value':tag.split(' ')[1][:-1] , 'type': 'string', 'unit': {'isSI': isSI, 'unit': ''}}
						
						
						rdfTest.appendOwlFile(filename,targetIRI,value,True)		
						return 0
						
					else:
						return 0
			
			elif(type == '3'):
				print('nameOfRow',nameOfRow)
				if('MAU' in EquipmentName):
					print('----------------------')
					id = re.search(idRegex,EquipmentName).group(0)
					filename = (filenameTemplate%id)
					if('%s' in IRITemplate):
						targetIRI = (IRITemplate %id)
					else:
						targetIRI = IRITemplate
					print('filename--- ',filename)
					print('targetIRI---	',targetIRI)
					#rdfTest.appendOwlFile(filename,targetIRI,value,True)
				else:
					return 0
					
			elif(type == '4'):
				print('nameOfRow',nameOfRow)
				if('MAU' in EquipmentName or 'FCU' in EquipmentName or 'RAHU' in EquipmentName):
					print('----------------------')
					id = re.search(idRegex,EquipmentName).group(0)[1:]
					filename = (filenameTemplate%id)
					if('%s' in IRITemplate):
						targetIRI = (IRITemplate %id)
					else:
						targetIRI = IRITemplate
					print('filename--- ',filename)
					print('targetIRI---	',targetIRI)
					#rdfTest.appendOwlFile(filename,targetIRI,value,True)
				else:
					return 0		
					
			elif(type == '5'):

				filename = (filenameTemplate %idRegex)
				targetIRI = (IRITemplate)
				print('filename',filename)
				print('targetIRI',targetIRI)

				
			elif(type == '6'):
			
			#VAV-%s.owl,						V_%s_Temperature_SP,
			
				print('currentVAV', currentVAV)
				id = re.search(idRegex, currentVAV).group(0)
				filename = (filenameTemplate %id).replace('_', '-')
				IRI = abbrPairs[currentRoomName]
				targetIRI = (IRITemplate %IRI)
				print('id',id)
				print('filename',filename)
				print('IRI',IRI)
				print('targetIRI',targetIRI)					
					
				
			# For specific devices, e.g. RAHU-Fan-7-1_A
			# Able to insert 'Fan', 'He' into filename
			# In the future, type 3 should be modified so type 7 can be partially replaced
			elif(type == '7'):
				# 1. Indentify device name , for example RAHU-7-1, in col[2] idRegex 
				if(idRegex in EquipmentName):
					print('Here we are', EquipmentName)
					filename = filenameTemplate
					targetIRI = IRITemplate
				else:
					return 0
				
				
				
			elif(type == '8'):
				# 1. Indentify device name , for example RAHU-7-1, in col[2] idRegex 
				
				
				# mapping for pipe names
				# mapping['FCU'] = {'0': ['D24','D29','D34','D39','D44','D49','D54'],
								  # '1': ['P20','P23','P26','P29','P32','P35','P38'], 
								  # '2': ['P22','P25','P28','P31','P34','P37','P40'], 
								  # '3': ['D27','D32','D37','D42','D47','D52','D57'],
								  # '4': 'P20', 
								  #	'5': 'P22',
								  #'6': 'P20'}
				if((idRegex in EquipmentName) or (idRegex in PanelBoard)):
				
					placeholderCount = IRITemplate.count('%s')
					if(placeholderCount == 1):
						id = EquipmentName[-1]
						filename = (filenameTemplate %id)
						targetIRI =(IRITemplate %id)
					else:
						if(EquipmentName):
							id = EquipmentName[-1]
						else:
							id = ''
						if(addOn):
							secondId = addOn[0] + str(int(addOn[1:]) + (int(id) - 1) * increment)
			
						if(filenameTemplate.count('%s') == 1):
							filename = (filenameTemplate %(id))
							targetIRI =(IRITemplate %(secondId,id))
							
						elif(filenameTemplate.count('%s') == 0):	
							filename = (filenameTemplate)
							targetIRI =(IRITemplate)
						else:
							filename = (filenameTemplate %(id,secondId))
							targetIRI =(IRITemplate %(secondId,id))
						
						
				else:
					return 0				
				
				
			# elif(type == '9'):
				# print('type 9')
			
			else:
				id	= (re.search(idRegex,tag)).group(0)
				filename = (filenameTemplate %id)
				targetIRI = (IRITemplate %id)
				
				
				
		
		# print('tag--- ',tag)
		# print('id--- ',id)
		# print('filename--- ',filename)
		# print('targetIRI---	',targetIRI)
		

		value = value.group(0).strip()[1:-1].strip() # cut off > and < leaving the value content only
		if(re.match(numericalValueRegex,value)):      # whether the value is a numerical value
			splittedArray = value.split(' ')			
			if(len(splittedArray)> 1):
				unit = splittedArray[1]
				value = splittedArray[0]
			else:
				unit = ''
		else:
			unit = ''
	else:
		value = ''
		unit = ''
	
	print('targetIRI -------', targetIRI)
	return {
		'content': {'value': value, 'type': 'float', 'unit': {'isSI': isSI, 'unit': unit}},
		'info'	 : {'filename': filename, 'targetIRI': targetIRI}
	
	}
	
	
# ===========Sample Mapping=============

# in xml : FH...Door re = 'WFH-[0-9]+_Door' (sufficient to identify)
# re = .*WFH-[0-9]+_Door.* to identify the whole line

# ===========================================================

# WFH-06_SashOp_sensor1 --- name of the owl file
# VAV_E7_14_WFH-06_Door --- name of the tag in xml raw data
# V_SashOpeningOfWFH-06 --- name of the IRI in the owl file 

abbrPairs = {}
heaterParams = ['HeaterControl', 'HeaterAirflow']


with open('workingdir/abbrPair.csv') as file:
	lines = file.readlines()
	for line in lines[1:]:
		cols = line.split(',')
		roomName = cols[0].strip()
		abbrName = cols[1].strip()
		abbrPairs[roomName] = abbrName
		
print(abbrPairs)
print('=======================================')

#input = input('stop')

with open('workingdir/backupmap3.csv') as file:
	lines = file.readlines()
	for line in lines[1:]: 
		cols = line.split(',')
		if(len(cols) > 3):
			nameOfRow 			= cols[0].strip()
			nameRegex 			= cols[1].strip()
			idRegex	  			= cols[2].strip()
			filenameTemplate 	= cols[3].strip()
			IRITemplate			= cols[4].strip()
			isSIUnit			= (int(cols[5].strip()) > 0) 
			abbrPair			= (int(cols[6].strip()) > 0)
			type 				= cols[8].strip()
			addOn				= cols[9].strip()
			if(cols[10].strip() == ''):
				increment =	0
			else:
				increment 		= int(cols[10].strip())
				
				
			currentRoomName = ''
			heaterAirflowIdTag = ''
			EquipmentName = ''
			PanelBoard = ''
			valueRegex = '>.*<'
	# Read local xml file
			with open('workingdir/bmsResult.html') as file:
				lines = file.readlines()
				for line in lines:
					if('<Area>' in line):
						
						currentRoomName = re.search(valueRegex,line).group(0)[1:-1].strip()
						
					if('<Equipment>' in line):
						EquipmentName = re.search(valueRegex,line).group(0)[1:-1].strip()
					
					if(('<Distribution_Board>' in line) or ('<Panel_Board>' in line)):
						PanelBoard = re.search(valueRegex,line).group(0)[1:-1].strip()
					
					
					
					heaterAirflowIdTagRegex = '<VAV_S[0-9]_[0-9]*.*'
				
					if(re.match(heaterAirflowIdTagRegex,line)):
						heaterAirflowIdTag = re.search('VAV_S[0-9]_[0-9]*',line).group(0).strip()
						
					if(re.match(nameRegex,line)):
						# inconsistent occurance # 001 
						line = line.replace('FH0','FH-0')
						#idRegex = 'W*FH-*[0-9]*'
						#idRegex2= '[a-zA-Z]*_[a-zA-Z][0-9]_[0-9]*'
						#filenameTemplate = ('%s.owl')
						#filenameTemplate2 = ('%s_SashOp_sensor1.owl')
						#IRITemplate = 'V_SashOpeningOf%s'
						value = seperateContent(line,idRegex,filenameTemplate,IRITemplate,True,nameOfRow)
						

						if(value != 0):
							filename = value['info']['filename']
							targetIRI= value['info']['targetIRI']
							rdfTest.appendOwlFile(filename,targetIRI,value['content'],False)
							#owlfileMatch = ('WFH-%s_SashOp_sensor1' %id)
							print('========================================')
							print()
					
					# value = {'value': 10.01, 'type': 'float', 'unit': {'isSI': True, 'unit': 'CM'}}
					# filename = 'WFH-07_SashOp_sensor1'
					# targetIRI = 'V_SashOpeningOfWFH-07'
					# appendOwlFile(filename,targetIRI,value)
					
		
			
			

		