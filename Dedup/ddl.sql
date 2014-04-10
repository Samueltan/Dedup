CREATE TABLE [mapping] (
[filename] VARCHAR(32)  NOT NULL,
[seqid] INTEGER  NULL,
[hashid] INTEGER  NULL,
[folder] VARCHAR(32)  NULL,
FOREIGN KEY ([hashid]) REFERENCES [hashes]([id])
);
CREATE TABLE [hashes] (
[id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,
[string] BLOB  NULL,
[hash] CHAR(16)  NOT NULL
);