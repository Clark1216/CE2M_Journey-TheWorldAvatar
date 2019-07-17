class Apl(object):
    def __init__(self):
        self.__header = None
        self.__sup = None
        self.__met = None
        self.__hil = None
        self.__cst = None
        self.__flc = None
        self.__grd = None
        self.__puf = None
        self.__gam = None
        self.__bkg = None
        self.__etc = None
        self.__chm = None
        self.__mapper = None

    def set_header(self, header):
        self.__header = header

    def set_sup(self, sup):
        self.__sup = sup

    def set_met(self, met):
        self.__met = met

    def set_hil(self, hil):
        self.__hil = hil

    def set_cst(self, cst):
        self.__cst = cst

    def set_flc(self, flc):
        self.__flc = flc

    def set_grd(self, grd):
        self.__grd = grd

    def set_puf(self, puf):
        self.__puf = puf

    def set_gam(self, gam):
        self.__gam = gam

    def set_bkg(self, bkg):
        self.__bkg = bkg

    def set_chm(self, chm):
        self.__chm = chm

    def set_etc(self, etc):
        self.__etc = etc

    def set_mapper(self, mapper):
        self.__mapper = mapper

    def specification(self):
        spec = self.__header.to_string()
        spec = spec + self.__sup.to_string()
        spec = spec + self.__met.to_string()
        spec = spec + self.__hil.to_string()
        spec = spec + self.__cst.to_string()
        spec = spec + self.__flc.to_string()
        spec = spec + self.__grd.to_string()
        spec = spec + self.__puf.to_string()
        spec = spec + self.__gam.to_string()
        spec = spec + self.__bkg.to_string()
        spec = spec + self.__chm.to_string()
        spec = spec + self.__etc.to_string()
        spec = spec + self.__mapper.to_string()
        return spec


class AplPart(object):
    def __init__(self):
        self.__name = '&'
        self.__end = '/'

    def to_string(self):
        str_out = self._name + '\n'

        for var_name in vars(self).keys():
            if not var_name.startswith('_'):
                str_out = str_out + var_name + ' = '
                var_val = vars(self)[var_name]
                if type(var_val) == str:
                    var_val = '"' + var_val + '"'
                elif type(var_val) == int or type(var_val) == float:
                    var_val = str(var_val)
                elif type(var_val) == list:
                    i = 0
                    out_val = '\n  '
                    for v in var_val:
                        if i != 0 and i % 4 == 0:
                            out_val = out_val + '\n  '
                        out_val = out_val + str('{:.1e}'.format(v)) + ' '
                        i = i + 1
                    var_val = out_val
                str_out = str_out + var_val + '\n'
        str_out = str_out + self._AplPart__end + '\n\n'

        return str_out


class AdmsHeader(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_HEADER'
        self.Comment = "This is an ADMS parameter file"
        self.Model = "ADMS"
        self.Version = 5.2
        self.FileVersion = 8
        self.Complete = 1


class AdmsSup(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_SUP'
        self.SupSiteName = "terrain dispersion site"
        self.SupProjectName = "chlorine leakage tank dispersion"
        self.SupUseAddInput = 0
        self.SupAddInputPath = " "
        self.SupReleaseType = 0
        self.SupModelBuildings = 1
        self.SupModelComplexTerrain = 1
        self.SupModelCoastline = 0
        self.SupPufType = 0
        self.SupCalcChm = 1
        self.SupCalcDryDep = 0
        self.SupCalcWetDep = 1
        self.SupCalcPlumeVisibility = 1
        self.SupModelFluctuations = 0
        self.SupModelRadioactivity = 0
        self.SupModelOdours = 0
        self.SupOdourUnits = "ou_e"
        self.SupPaletteType = 1
        self.SupUseTimeVaryingEmissions = 0
        self.SupTimeVaryingEmissionsType = 0
        self.SupTimeVaryingVARPath = " "
        self.SupTimeVaryingFACPath = " "
        self.SupTimeVaryingEmissionFactorsWeekday = [1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0,
                                                     1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0,
                                                     1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0,
                                                     1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0]
        self.SupTimeVaryingEmissionFactorsSaturday = [1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0,
                                                      1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0,
                                                      1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0,
                                                      1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0]
        self.SupTimeVaryingEmissionFactorsSunday = [1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0,
                                                    1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0,
                                                    1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0,
                                                    1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0, 1.0e+0]


class AdmsMet(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_MET'
        self.MetLatitude = 0
        self.MetDataSource = 0
        self.MetDataFileWellFormedPath = " "
        self.MetWindHeight = 1.0e+1
        self.MetWindInSectors = 0
        self.MetWindSectorSizeDegrees = 1.0e+1
        self.MetDataIsSequential = 0
        self.MetUseSubset = 0
        self.MetSubsetHourStart = 1
        self.MetSubsetDayStart = 1
        self.MetSubsetMonthStart = 1
        self.MetSubsetYearStart = 2016
        self.MetSubsetHourEnd = 0
        self.MetSubsetDayEnd = 1
        self.MetSubsetMonthEnd = 1
        self.MetSubsetYearEnd = 2017
        self.MetUseVerticalProfile = 0
        self.MetVerticalProfilePath = " "
        self.Met_DS_RoughnessMode = 1
        self.Met_DS_Roughness = 1.0e+0
        self.Met_DS_UseAdvancedMet = 0
        self.Met_DS_SurfaceAlbedoMode = 1
        self.Met_DS_SurfaceAlbedo = 2.3e-1
        self.Met_DS_PriestlyTaylorMode = 1
        self.Met_DS_PriestlyTaylor = 1.0e+0
        self.Met_DS_MinLmoMode = 1
        self.Met_DS_MinLmo = 3.45e+1
        self.Met_DS_PrecipFactorMode = 1
        self.Met_DS_PrecipFactor = 4.5e-1
        self.Met_MS_RoughnessMode = 3
        self.Met_MS_Roughness = 1.0e-1
        self.Met_MS_UseAdvancedMet = 0
        self.Met_MS_SurfaceAlbedoMode = 3
        self.Met_MS_SurfaceAlbedo = 2.3e-1
        self.Met_MS_PriestlyTaylorMode = 3
        self.Met_MS_PriestlyTaylor = 1.0e+0
        self.Met_MS_MinLmoMode = 3
        self.Met_MS_MinLmo = 1.0e+0
        self.MetHeatFluxType = 0
        self.MetInclBoundaryLyrHt = 0
        self.MetInclSurfaceTemp = 1
        self.MetInclLateralSpread = 0
        self.MetInclRelHumidity = 0
        self.MetHandNumEntries = 1
        self.MetWindSpeed = [3.06e+0]
        self.MetWindDirection = [6.0e+1]
        self.MetJulianDayNum = [2.47e+2]
        self.MetLocalTime = [5.0e+0]
        self.MetCloudAmount = [5.0e+0]
        self.MetSurfaceHeatFlux = [0.0e+0]
        self.MetBoundaryLayerHeight = [8.00e+2]
        self.MetSurfaceTemp = [2.8e+1]
        self.MetLateralSpread = [7.5e+0]
        self.MetYear = [2017]
        self.MetRelHumidity = [7.4e+1]


class AdmsHil(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_HIL'
        self.HilGridSize = 2
        self.HilUseTerFile = 1
        self.HilUseRoughFile = 0
        self.HilTerrainPath = " "
        self.HilRoughPath = " "
        self.HilCreateFlowField = 1


class AdmsCst(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_CST'
        self.CstPoint1X = 0.0e+0
        self.CstPoint1Y = 0.0e+0
        self.CstPoint2X = -1.000e+3
        self.CstPoint2Y = 1.000e+3
        self.CstLandPointX = 5.00e+2
        self.CstLandPointY = 5.00e+2


class AdmsFlc(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_FLC'
        self.FlcAvgTime = 9.00e+2
        self.FlcUnitsPollutants = "ug/m3"
        self.FlcUnitsIsotopes = "Bq/m3"
        self.FlcCalcToxicResponse = 0
        self.FlcToxicExp = 1.0e+0
        self.FlcCalcPercentiles = 0
        self.FlcNumPercentiles = 0
        self.FlcCalcPDF = 0
        self.FlcPDFMode = 0
        self.FlcNumPDF = 0


class AdmsGrd(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_GRD'
        self.GrdType = 0
        self.GrdCoordSysType = 0
        self.GrdSpacingType = 0
        self.GrdRegularMin = [0.00e+0, 0.00e+0, 0.00e+0, 1.0e+1, 0.0e+0, 0.0e+0]
        self.GrdRegularMax = [0.00e+0, 0.00e+0, 3.00e+1, 1.000e+3, 3.30e+2, 0.0e+0]
        self.GrdRegularNumPoints = [80, 80, 4, 10, 12, 1]
        self.GrdVarSpaceNumPointsX = 0
        self.GrdVarSpaceNumPointsY = 0
        self.GrdVarSpaceNumPointsZ = 0
        self.GrdVarSpaceNumPointsR = 0
        self.GrdVarSpaceNumPointsTh = 0
        self.GrdVarSpaceNumPointsZp = 0
        self.GrdPtsNumPoints = "0 0"
        self.GrdPolarCentreX = 0.0e+0
        self.GrdPolarCentreY = 0.0e+0
        self.GrdPtsUsePointsFile = 1
        self.GrdPtsPointsFilePath = " "


class AdmsPuf(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_PUF'
        self.PufStart = 1.00e+2
        self.PufStep = 1.00e+2
        self.PufNumSteps = 10


class AdmsGam(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_GAM'
        self.GamCalcDose = 0


class AdmsBkg(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_BKG'
        self.BkgFilePath = " "
        self.BkgFixedLevels = 0


class AdmsEtc(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_ETC'
        self.SrcNumSources = 1
        self.PolNumPollutants = 19
        self.PolNumIsotopes = 0


class AdmsChm(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_PARAMETERS_CHM'
        self.ChmScheme = 2


class AdmsMapper(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + 'ADMS_MAPPERPROJECT'
        self.ProjectFilePath = " "


class AdmsPold(AplPart):
    def __init__(self):
        super().__init__()
        self._name = self._AplPart__name + '&ADMS_POLLUTANT_DETAILS'
        self.PolName = "CO2"
        self.PolPollutantType = 0
        self.PolGasDepVelocityKnown = 1
        self.PolGasDepositionVelocity = 0.0e+0
        self.PolGasType = 1
        self.PolParDepVelocityKnown = 1
        self.PolParTermVelocityKnown = 1
        self.PolParNumDepositionData = 1
        self.PolParDepositionVelocity = [0.0e+0]
        self.PolParTerminalVelocity = [0.0e+0]
        self.PolParDiameter = [1.0e-6]
        self.PolParDensity = [1.000e+3]
        self.PolParMassFraction = [1.0e+0]
        self.PolWetWashoutKnown = 0
        self.PolWetWashout = 0.0e+0
        self.PolWetWashoutA = 1.0e-4
        self.PolWetWashoutB = 6.4e-1
        self.PolConvFactor = 5.47e-1
        self.PolBkgLevel = 4.14e+5
        self.PolBkgUnits = "ppb"
