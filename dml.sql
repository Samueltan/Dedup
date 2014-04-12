insert into hashes(string, hash) values("hello world\n", "<hash1>");
insert into hashes(string, hash) values("hello samuel\n", "<hash2>");
insert into hashes(string, hash) values("hello gump\n", "<hash3>");
insert into hashes(string, hash) values("hello nicole\n", "<hash4>");
insert into hashes(string, hash) values("hello terry\n", "<hash5>");
insert into hashes(string, hash) values("hello lynn\n", "<hash6>");

insert into mapping(filename, seqid, hashid) values("file1.txt", 1, 1);
insert into mapping(filename, seqid, hashid) values("file1.txt", 2, 2);
insert into mapping(filename, seqid, hashid) values("file1.txt", 3, 3);
insert into mapping(filename, seqid, hashid) values("file2.txt", 1, 1);
insert into mapping(filename, seqid, hashid) values("file2.txt", 2, 2);
insert into mapping(filename, seqid, hashid) values("file2.txt", 3, 4);
insert into mapping(filename, seqid, hashid) values("file2.txt", 4, 6);