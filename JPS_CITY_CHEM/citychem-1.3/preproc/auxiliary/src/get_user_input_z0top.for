! <get_user_input_z0top.for  - A component of the City-scale
!                 Chemistry Transport Model EPISODE-CityChem>
!*****************************************************************************!
!* 
!* EPISODE - An urban-scale air quality model
!* ========================================== 
!* Copyright (C) 2018  NILU - Norwegian Institute for Air Research
!*                     Instituttveien 18
!*                     PO Box 100
!*                     NO-2027 Kjeller
!*                     Norway
!*
!*                     Contact persons: Gabriela Sousa Santos - gss@nilu.no
!*                                      Paul Hamer - pdh@nilu.no
!*
!* Unless explicitly acquired and licensed from Licensor under another license,
!* the contents of this file are subject to the Reciprocal Public License ("RPL")
!* Version 1.5, https://opensource.org/licenses/RPL-1.5 or subsequent versions as
!* allowed by the RPL, and You may not copy or use this file in either source code
!* or executable form, except in compliance with the terms and conditions of the RPL. 
!*
!* All software distributed under the RPL is provided strictly on an "AS IS" basis, 
!* WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY 
!* DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF
!* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT.
!* See the RPL for specific language governing rights and limitations under the RPL.
!*
!* ========================================== 
!* The dispersion model EPISODE (Grønskei et. al., 1993; Larssen et al., 1994;
!* Walker et al., 1992, 1999; Slørdal et al., 2003, 2008) is an Eulerian grid model 
!* with embedded subgrid models for calculations of pollutant concentrations resulting 
!* from different types of sources (area-, line- and point sources). EPISODE solves the 
!* time dependent advection/-diffusion equation on a 3 dimensional grid. 
!* Finite difference numerical methods are applied to integrate the solution forward in time. 
!* It also includes extensions as the implementation of a simplified EMEP photochemistry 
!* scheme for urban areas (Walker et al. 2004) and a scheme for Secondary Organic Aerosol 
!* implemented by Håvard Slørdal
!*
!* Grønskei, K.E., Walker, S.E., Gram, F. (1993) Evaluation of a model for hourly spatial
!*    concentrations distributions. Atmos. Environ., 27B, 105-120.
!* Larssen, S., Grønskei, K.E., Gram, F., Hagen, L.O., Walker, S.E. (1994) Verification of 
!*    urban scale time-dependent dispersion model with sub-grid elements in Oslo, Norway. 
!*    In: Air poll. modelling and its appl. X. New York, Plenum Press.
!* Slørdal, L.H., McInnes, H., Krognes, T. (2008): The Air Quality Information System AirQUIS. 
!*    Info. Techn. Environ. Eng., 1, 40-47, 2008.
!* Slørdal, L.H., Walker, S.-E., Solberg, S. (2003) The Urban Air Dispersion Model EPISODE 
!*    applied in AirQUIS. Technical Description. NILU TR 12/2003. ISBN 82-425-1522-0.
!* Walker, S.E., Grønskei, K.E. (1992) Spredningsberegninger for on-line overvåking i Grenland. 
!*    Programbeskrivelse og brukerveiledning. Lillestrøm, 
!*    Norwegian Institute for Air Research (NILU OR 55/92).
!* Walker, S.E., Slørdal, L.H., Guerreiro, C., Gram, F., Grønskei, K.E. (1999) Air pollution 
!*    exposure monitoring and estimation. Part II. Model evaluation and population exposure. 
!*    J. Environ. Monit, 1, 321-326.
!* Walker, S.-E., Solberg, S., Denby, B. (2003) Development and implementation of a simplified 
!*    EMEP photochemistry scheme for urban areas in EPISODE. NILU TR 13/2013. 
!*    ISBN 82-425-1524-7
!*
!* ========================================== 
!*
!***********************************************************************
!***
!***      User meta info input file for EPISODE-CityChem
!***
!***********************************************************************

      subroutine get_user_input

!***********************************************************************
!***  Subroutine get_user_input reads the meta information
!***  for preparation of input files for CityChem extension
!***********************************************************************

!***  NOTE: THE META INFORMATION MAY BE EXTENDED FOR METEO AND BCON

!     Declarations of variables by using the MODULES feature:

      use module_cc_input


      implicit none

!***********************************************************************

!     Local declarations:

      integer :: i
      integer :: word_len
      
!***********************************************************************
!     Content of subroutine:


!     Definition of some simple input and output files:

      funit_run  = 10
      fname_run  = 'cctapm_meta.inp'

      open (unit=funit_run, file=fname_run,status='old')

!     Start reading parameters from the meta info file:

!     SIM-ID
      read(funit_run,*) simid

!     Various file-names:
!
!     Input path for TAPM and CMAQ data (not used)
      read(funit_run,*) fname_inpath_tapm
      read(funit_run,*) fname_inpath_cmaq


!     Check if files exists
      INQUIRE(FILE=fname_inpath_tapm,EXIST = fe_inpath_tapm)
      INQUIRE(FILE=fname_inpath_cmaq,EXIST = fe_inpath_cmaq)

!     Files containing input-data:
      read(funit_run,*) fname_in_points
      read(funit_run,*) fname_in_lines
      read(funit_run,*) fname_in_area_sector
!
!     Check if files exists
      INQUIRE(FILE=fname_in_points,EXIST = fe_in_points)
      INQUIRE(FILE=fname_in_lines,EXIST = fe_in_lines)
      INQUIRE(FILE=fname_in_area_sector,EXIST = fe_in_area_sector)

! IF ANY OF THESE IS MISSING STOP THE PROGRAM

!     Files containing output-data:
      read(funit_run,*) fname_outpath

!     Log file
      read(funit_run,*) fname_log

!     Selected model
      read(funit_run,*) model

      if ((model.ne.'CC').and.(model.ne.'TP')) then
        call stopit('Indicate which model: CC or TP')
      endif

!     Selected output
      read(funit_run,*) source
      if ((source.ne.'PSE').and.(source.ne.'LSE').and.  &
          (source.ne.'ASE').and.(source.ne.'ALL')) then
        call stopit('Enter output type: PSE, LSE, ASE or ALL')
      endif

!     ASCII or Binary
      read(funit_run,*) EP_fm

!     The number of hours to compute:
      read(funit_run,*) hh

!     Start/End date of input
      read(funit_run,*) startdate
      read(funit_run,*) enddate

      read(funit_run,*)  (bdat(i),i=1,3)
      read(funit_run,*)  (edat(i),i=1,3)

!     Model dimensions:
      read(funit_run,*) nx
      read(funit_run,*) ny

!     Horizontal grid resolution (in meters):
      read(funit_run,*) dxarea   ! gridded area source
      read(funit_run,*) dxout    ! output grid

      if (dxarea > dxout) then
        call stopit('Area emission resolution has to be <= model resolution')
      endif

!     Grid origo
      read(funit_run,*) sitex0,sitey0

      if (sitex0 > 1.e6) then
        call stopit('Enter truncate UTM x-coordinate (no leading digits for UTM zone)')
      endif

!     Grid UTM zone
      read(funit_run,*) utmzone

!     Number of sources in the input files:
      read(funit_run,*) n_sopp
      read(funit_run,*) n_soll
      read(funit_run,*) n_soaa

      if (n_sopp > 9999) then
        call stopit('UECT allows max. 9999 point sources')
      endif

!     Optional netCDF output for checking
      read(funit_run,*) NC_out

!     Construct the begin date
!     Set start time and day of week
      bdat(4) = 0               ! hour(0-23)
      bdat(5) = 0
      bdat(6) = 0
      edat(4) = 23              ! hour(0-23)
      edat(5) = 0
      edat(6) = 0
      year = bdat(1)
      mnth = bdat(2)
      daym = bdat(3)
      hour = bdat(4)
      minu = bdat(5)
      seco = bdat(6)



!     Allocate statements here? Rather in seperate module (and free memory)
!        if (.not. allocated (surface_stat_dt)) &
!              allocate (surface_stat_dt(n_surface_stat))
!
!        if (.not. allocated (surface_stat_sr)) &
!              allocate (surface_stat_sr(n_surface_stat))    
!
!        if (.not. allocated (surface_stat_press)) &
!              allocate (surface_stat_press(n_surface_stat))  
!
!        if (.not. allocated (surface_stat_rh)) &
!              allocate (surface_stat_rh(n_surface_stat))
!
!        if (.not. allocated (surface_stat_mm)) &
!              allocate (surface_stat_mm(n_surface_stat))
!
!        if (.not. allocated (surface_stat_clc)) &
!              allocate (surface_stat_clc(n_surface_stat))
!
!        if (.not. allocated (surface_stat_z0)) &
!              allocate (surface_stat_z0(n_surface_stat))
!
!        if (.not. allocated (surface_stat_pwr)) &
!              allocate (surface_stat_pwr(n_surface_stat))
!
!        if (.not. allocated (surface_stat_scale)) &
!              allocate (surface_stat_scale(n_surface_stat))
!
!        if (.not. allocated (app_surface_stat_scale)) &
!              allocate (app_surface_stat_scale(n_surface_stat))
!
!        if (.not. allocated (surface_stat_ffref)) &
!              allocate (surface_stat_ffref(n_surface_stat))       


      close(funit_run)

! **********************************************************************

!     Write the Log file:

      funit_log  = 11
      
      fe_log     = .false.
      if (fname_log(1:1) /= ' ') then
        open (unit=funit_log, file=fname_log,status='unknown')
        fe_log = .true.
      end if

      if (fe_log) then
        write(funit_log,'(1X,A)')         &
    '************************************************************'

        word_len = LEN_TRIM(fname_run)
        write(funit_log,'(1X,3A)') &
    'Input parameters read from the RUN_FILE: "', &
    fname_run(1:word_len),'"'

        write(funit_log,'(1X,A)') &
    '************************************************************'

        write(funit_log,*)
        write(funit_log,'(1X,A)') 'Files containing INPUT-data: '
        write(funit_log,'(1X,A)') '**************************** '
        write(funit_log,*)

!        word_len = LEN_TRIM(fname_in_top)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the input TOPOGRAPHY_FILE = "', &
!    fname_in_top(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_in_landuse)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the input LANDUSE_FILE = "', &
!    fname_in_landuse(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_in_surfrough)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the input SURF_ROUGHNESS_FILE = "', &
!    fname_in_surfrough(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_in_surface_obs)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the input MET_SURFACE_OBSERVATION_FILE = "', &
!    fname_in_surface_obs(1:word_len),'"' 
!
!        word_len = LEN_TRIM(fname_in_profile_obs)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the input MET_PROFILE_OBSERVATION_FILE = "', &
!    fname_in_profile_obs(1:word_len),'"' 
!
!        word_len = LEN_TRIM(fname_in_geostrophic_obs)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the input MET_GEOSTROPHIC_OBSERVATION_FILE = "', &
!    fname_in_geostrophic_obs(1:word_len),'"'
!
        write(funit_log,*)
        write(funit_log,'(1X,A)') 'Files containing OUTPUT-data: '
        write(funit_log,'(1X,A)') '***************************** '
        write(funit_log,*)

        word_len = LEN_TRIM(fname_log)
        write(funit_log,'(1X,A58,2A)')  &
    'The name of the applied LOG_FILE = "', &
    fname_log(1:word_len),'"'

!        word_len = LEN_TRIM(fname_top)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the output TOPOGRAPHY_FILE = "', &
!    fname_top(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_wind_res)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the WIND RESULT_FILE = "', &
!    fname_wind_res(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_temp_res)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the TEMP RESULT_FILE = "', &
!    fname_temp_res(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_tsrad_res)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the output TSRAD_FILE = "', &
!    fname_tsrad_res(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_albedo_res)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the output ALBEDO_FILE = "', &
!    fname_albedo_res(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_nrad_res)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the output NRAD_FILE = "', &
!    fname_nrad_res(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_pres_res)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the output PRESSURE_FILE = "', &
!    fname_pres_res(1:word_len),'"'
!
!        word_len = LEN_TRIM(fname_tsmet)
!        write(funit_log,'(1X,A58,2A)')  &
!    'The name of the output TSMET_FILE = "', &
!    fname_tsmet(1:word_len),'"'

        write(funit_log,*)
        write(funit_log,'(1X,A)') 'User-defined steering parameters: '
        write(funit_log,'(1X,A)') '********************************* '
        write(funit_log,*)

!        write(funit_log,'(1X,A,I5)')  &
!    'Number of fields (hours) to be calculated = ',n_max
!        write(funit_log,*)
!        write(funit_log,'(1X,A,I5)')  &
!    'West-East   model dimension = ',im
!        write(funit_log,'(1X,A,I5)')  &
!    'South-North model dimension = ',jm
!        write(funit_log,'(1X,A,I5)')  &
!    'Ground-Top  model dimension = ',km
!        write(funit_log,*)
!        write(funit_log,'(1X,A,F8.2)')  &
!    'West-East   grid spacing in meters       = ',dx
!        write(funit_log,'(1X,A,F8.2)')  &
!    'South-North grid spacing in meters       = ',dy
!!_LHS_Aug2007_Start:
!        write(funit_log,'(1X,A,F8.2)')  &
!    'Depth of lowermost sigma layer in meters = ',first_layer_depth
!        write(funit_log,'(1X,A,F8.4)')  &
!    'Stretch factor for the above layers      = ',stretch_factor
!        write(funit_log,*)
!        write(funit_log,*) ' If the stretch_factor is negative, then: '
!	  do n = 1,km
!        write(funit_log,'(1X,A21,I3,A13,F8.2)')  &
!    'Depth of sigma layer ',n,' in meters = ', &
!     user_def_deltasigma(n)
!	  end do
!        write(funit_log,*)
        
        write(funit_log,'(1X,A)') &
    'END of input parameters read from the user-supplied META info:'
        write(funit_log,'(1X,A)') &
    '************************************************************'
      end if



      return
      end subroutine get_user_input
