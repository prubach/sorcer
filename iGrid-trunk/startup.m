% Matlab startup script

% Generic Matlab Utility Routines
addpath( fullfile( pwd, 'modules', 'engineering', 'matlabUtil' ) );

% ModelClientGui
addpath( fullfile( pwd, 'modules', 'sorcer', 'src', 'sorcer', 'client', 'ModelClientGui' ) );

% QsCsd pre/post processing utilities
addpath( fullfile( pwd, 'data', 'matlab_example', 'qscsd' ) );
addpath( fullfile( pwd, 'data', 'matlab_example', 'qscsd', 'pre' ) );
addpath( fullfile( pwd, 'data', 'matlab_example', 'qscsd', 'post' ) );
addpath( fullfile( pwd, 'data', 'matlab_example', 'qscsd', 'bin' ) );

% MSTCGA DataBase
addpath( fullfile( pwd, 'modules', 'engineering', 'optimization', 'matlab', 'library', 'src', 'mstcga' ) );

% MSTCGA DataBase Gui
addpath( fullfile( pwd, 'modules', 'engineering', 'optimization', 'matlab', 'library', 'src', 'ExploreDatabaseGui' ) );

