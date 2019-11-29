import numpy as np
import pandas as pd
import requests
from firebase import firebase

#from googlegeocoder import GoogleGeocoder
#import geocoder
#from geopy.geocoders import Nominatim
#geolocator = Nominatim()

#Dublin City Counsil have an api page for their sound readings
#https://data.smartdublin.ie/
#http://dublincitynoise.sonitussystems.com/applications/api/api-doc.html



#API Documentation 
#Return location data
#Parameters: returnLocationStrings, location.

# passing parameters to the script using either GET or POST methods.

def location():
    
    params = {"returnLocationStrings":True ,"location":"all" }
    
    url="http://dublincitynoise.sonitussystems.com/applications/api/dublinnoisedata.php"
    location_get_request = requests.get(url,params=params)

    location = location_get_request.json()
    #string = ' ,Ireland'
    #location2 = [x + string for x in location]         #string appended to json causes problems
    return location

locations = location()
print(locations)


"""   
https://geocoder.readthedocs.io/api.html#forward-geocoding getting one coordinte other is nonetype error not sure why
https://github.com/geopy/geopy
https://geocoder.readthedocs.io/api.html#forward-geocoding

def long_lat(location):
        
    lat = []
    lng = []
    for x in location:
        #print(x)
        g = geolocator.geocode(x)
        lng= [g.longitude]
        lat=  [g.latitude]
   
    

    return lat,long

print(long_lat(location)) 
    #seems the need to register and more, easier to use google
    lat = []
    lng = []
    for x in location:
         g = geocoder.geonames(x,key='AIzaSyAfVtXbuxYgBR12JQlpc-s_EFFFZ1it5-o')
         print(g.json)
         lng= [g.longitude]
         lat=  [g.latitude]
   
    

    return lat,long


"""

##Use the google API

def getLong_Lat(location):
    
    url = 'https://maps.googleapis.com/maps/api/geocode/json'
    lat = np.zeros(np.shape(location)[0])
    long = np.zeros(np.shape(location)[0])
    y=0
    
    for x in location:
        paramaters = {'address':x+',Dublin', 'key':'AIzaSyAfVtXbuxYgBR12JQlpc-s_EFFFZ1it5-o'}
        r = requests.get(url, params=paramaters)
        
        
        long_lat = r.json()
    
        results = long_lat['results'][0]
        
        lat[y] = results['geometry']['location']['lat']
        long[y] = results['geometry']['location']['lng']
        y+=1
        
    return lat,long


"""
Return sound level data
Parameters: location, start, end
location - an integer from 1 to 12 representing the site you wish to query. Defaults to 1.
start - the start date of the measurement time frame you wish to query. Must be a unix timestamp in seconds since the Epoch (January 1 1970 00:00:00 GMT). Defaults to the timestamp of the last measurement in the database.
end - the end date of the measurement time frame you wish to query. Must be a unix timestamp in seconds since the Epoch (January 1 1970 00:00:00 GMT). Defaults to the timestamp of the last measurement in the database.


"""




def return_sound_level(location,lat,long,start= '2019-10-1 00:00:00',end= '2019-10-10 00:00:00'):
   
    start = pd.Timestamp(start)
    start = start.value // 10 ** 9 

    end = pd.Timestamp(end)
    end = end.value // 10 ** 9


    params = {"location":1,"start":start,"end":end}
    
    url="http://dublincitynoise.sonitussystems.com/applications/api/dublinnoisedata.php"
    
    start_end_request = requests.get(url,params=params)

    time_date_json = start_end_request.json()

    
    
    times = time_date_json['times']
    dates = time_date_json['dates']
    noise = np.array(time_date_json['aleq'],dtype='float')

    date_time=pd.DataFrame({'date':dates,'time':times})

    dataset = pd.DataFrame({'Date':pd.to_datetime(date_time['date'] + ' ' + date_time['time'],dayfirst = True),'Noise':noise})

    dataset['Location'] = location[0]

    dataset['Lat'] = lat[0]
    dataset['Long'] = long[0]

    
    for x in np.arange(2,np.shape(location)[0]+1):

        new_parm = {'location':x,'start':start,'end':end}

        location_data_request = requests.get(url,params=new_parm)

        location_data_json = location_data_request.json()


        noise_new = np.array(location_data_json['aleq'],dtype='float')

        times = location_data_json['times']
        dates = location_data_json['dates']

        date_time_new=pd.DataFrame({'date':dates,'time':times})

        dataset2 = pd.DataFrame({'Date':pd.to_datetime(date_time_new['date'] + ' ' + date_time_new['time'],dayfirst = True),'Noise':noise_new})


        dataset2['Location'] = location[x-1]
        dataset2['Lat'] = lat[x-1]
        dataset2['Long'] = long[x-1]

        dataset = dataset.append(dataset2)

    #dataset = dataset.reset_index()
    
    return dataset


############################### Call Functions ##################
loc = location()

lat,long = getLong_Lat(loc)
dataset = return_sound_level(loc,lat,long,start= '2019-10-01 00:00:00',end= '2019-10-10 00:00:00')

dataset.to_csv(path_or_buf = './dublin_noise_level.csv')

csv_dataset = pd.read_csv('./dublin_noise_level.csv')

csv_dataset.columns = ['Id','Data','Noise',
                     'contLocationinent','Lat','Long']

export_csv = csv_dataset.to_csv ('./Dublin_noise.csv', index = None, header=True)





import csv 
import json 


csvPath = './Dublin_noise.csv'
jsonPath = 'noise.json'

data = {}

csv_file = pd.DataFrame(pd.read_csv(csvPath, sep = ",", header = 0, index_col = False))
csv_file.to_json(jsonPath, orient = "records", date_format = "epoch", double_precision = 10, force_ascii = True, date_unit = "ms", default_handler = None)

#Another method of covnerting csv to json for firebase// works just as well 

#with open(csvPath) as csvFile:
#    csvReader = csv.DictReader(csvFile)
#    for csvrow in csvReader:
#       colid = csvrow["Id"]
#        data[colid] = csvrow
        
#with open(jsonPath,'w') as jsonFile:
#    jsonFile.write(json.dumps(data,indent=4))






