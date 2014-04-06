CREATE TABLE [mapping] (
[filename] VARCHAR(32)  NOT NULL,
[seqid] INTEGER  NULL,
[hashid] INTEGER  NULL,
[folder] VARCHAR(32)  NULL,
PRIMARY KEY ([filename],[seqid]),
FOREIGN KEY ([hashid]) REFERENCES [hashes]([id])
);
CREATE TABLE [hashes] (
[id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,
[string] VARCHAR(1024)  NULL,
[hash] VARCHAR(32)  NOT NULL
);