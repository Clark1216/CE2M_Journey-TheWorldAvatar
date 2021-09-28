
import pandas as pd

class Agent():

    def __init__(self):
        file = 'C:/my/tmp/ontomatch/municipalities_germany.csv'
        self.df_municip = pd.read_csv(file)
        print('loaded file=', file, ', number=', len(self.df_municip), ', columns=', self.df_municip.columns)

    def query(self, location:str, zipcode:int):

        found_row = None

        if zipcode:
            mask = (self.df_municip['zipcode'] == zipcode)
            df_tmp = self.df_municip[mask]
            if len(df_tmp) == 1:
                found_row = df_tmp.iloc[0]
            elif len(df_tmp) > 1:
                print('several entries found for zipcode=', zipcode)
                if location:
                    for _, row in df_tmp.iterrows():
                        token = row['location_normalized'].split()[0]
                        loc_norm = location.lower().strip()
                        if loc_norm.startswith(token):
                            found_row = row
                            break
                else:
                    found_row = df_tmp.iloc[0]

        elif location:
            loc_norm = location.replace('-',' ').lower().strip()
            mask = (self.df_municip['location_normalized'] == loc_norm)
            df_tmp = self.df_municip[mask]
            if len(df_tmp) == 1:
                print('single entry found for location=', loc_norm)
                found_row = df_tmp.iloc[0]
            elif len(df_tmp) > 1:
                print('several entries found for location=', loc_norm)
                found_row = df_tmp.iloc[0]

        if found_row is not None:
            return found_row['latitude'], found_row['longitude']
        print('no entries found for location=', location, ', zipcode=', zipcode)
        return None, None
