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