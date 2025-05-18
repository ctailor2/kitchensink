db.createUser(
        {
            user: "appUser",
            pwd: "appPassword",
            roles: [
                {
                    role: "readWrite",
                    db: "app"
                }
            ]
        }
);
db.member.createIndex({email: 1}, {unique: true})