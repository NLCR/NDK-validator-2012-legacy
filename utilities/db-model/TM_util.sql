-- UrnNbnDAO
CREATE table urn_nbn (
	id bigint IDENTITY(1,1)PRIMARY KEY CLUSTERED,
	registrar varchar(10),
	value varchar(50) unique,
	cdm_id varchar(50),
	assigned_datetime datetime
);

-- WaLogDAO
CREATE table wa_log (
	id bigint IDENTITY(1,1)PRIMARY KEY CLUSTERED,
	cdm_id varchar(50),
	files_in_wa int,
	created datetime
);

-- log for mule events
CREATE table log (
    id bigint IDENTITY(1,1)PRIMARY KEY CLUSTERED,
    processInstanceId varchar(10),
    nodeId varchar(50),
    eventType varchar(20),
    utilityName varchar(100),
    message varchar(250),
    exceptionWasThrown bit,
    duration bigint,
    created datetime
);