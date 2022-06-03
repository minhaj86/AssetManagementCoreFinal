INSERT INTO AssetEntity(id, title, metadata) VALUES (1, 'Matrix', '{"actor":"keanu reeves","yearOfRelease":"1999","producer":"Joel Silver","director":"Lana Wachowski,Lilly Wachowski"}');
INSERT INTO AssetEntity(id, title, metadata) VALUES (2, 'Lord of the rings','{"producer":"Peter Jackson","director":"peter jackson","actor":"Elijah Wood,Ian McKellen,Liv Tyler","yearOfRelease":"2001"}');
INSERT INTO AssetEntity(id, title, metadata) VALUES (3, 'Friends S1E1','{"actor":"jennifer anniston","producer":"Marta Kaufman","director":"Marta Kaufman","yearOfRelease":"1994"}');

INSERT INTO MediaEntity(id, fileName, filePath, asset_id) VALUES (1, 'matrix.mp4', '/Users/minhmdru/mediaroot/1/', 1);
INSERT INTO MediaEntity(id, fileName, filePath, asset_id) VALUES (2, 'matrix.jpg', '/Users/minhmdru/mediaroot/1/', 1);
INSERT INTO MediaEntity(id, fileName, filePath, asset_id) VALUES (3, 'LOTR.mxf', '/Users/minhmdru/mediaroot/2/', 2);
INSERT INTO MediaEntity(id, fileName, filePath, asset_id) VALUES (4, 'LOTR.jpg', '/Users/minhmdru/mediaroot/2/', 2);
INSERT INTO MediaEntity(id, fileName, filePath, asset_id) VALUES (5, 'friends_s1_e1.mxf', '/Users/minhmdru/mediaroot/3/', 3);
INSERT INTO MediaEntity(id, fileName, filePath, asset_id) VALUES (6, 'friends_s1_e1.jpg', '/Users/minhmdru/mediaroot/3/', 3);
INSERT INTO MediaEntity(id, fileName, filePath, asset_id) VALUES (7, 'matrix_MP4.mpd', '/Users/minhmdru/mediaroot/1/', 1);



