const cc = require('chance').Chance();
const roles = ['moderator', 'editor', 'player', 'subscriber', 'basic', 'owner', 'leader', 'employer', 'participant'] 
const users = [];
const normal = {
  account: ["manage-account"]
};
const admin = {
  "realm-management": ["realm-admin"],
  account: ["manage-account"]
};
let number = 1000;
while( number > 0 ) {
   number = number - 1;
   let clientRoles = normal;
   if (cc.rpg('1d100')[0] < 3) {
      clientRoles = admin;
   }

   const name = cc.name();
   const names = name.split(' ');
   user = {
     username: cc.word("abcdefgh"),
     enabled: true,
     email: cc.email(),
     firstName: names[0],
     lastName: names[1],
     credentials: [
       { type: "password",
         value: cc.string("123456789") } ],
     realmRoles: [ "user" ].concat(cc.pick(roles, cc.rpg('1d3')[0])),
     clientRoles: clientRoles
   };
   users.push(user);
}

const v4 = require('uuid').v4;
const exportRoles = [];
for (let role in roles) {
    exportRoles.push({
        id: v4(),
        name: role,
        description: '${role_' + role + '}',
        scopeParamRequired: true,
        composite: false,
        clientRole: false,
        containerId: "Practice"
      });
}


console.log(JSON.stringify({
  users: users,
  roles: exportRoles,
  groups: [
    {
      id: v4(),
      name: "TeamOne",
      path: "/TeamOne",
      attributes: {},
      realmRoles: [],
      clientRoles: {},
      subGroups: []
    },
    {
      id: v4(),
      name: "TeamTwo",
      path: "/TeamTwo",
      attributes: {},
      realmRoles: [],
      clientRoles: {},
      subGroups: []
    },
    {
      id: v4(),
      name: "TeamThree",
      path: "/TeamThree",
      attributes: {},
      realmRoles: [],
      clientRoles: {},
      subGroups: []
    },
    {
      id: v4(),
      name: "TeamFour",
      path: "/TeamFour",
      attributes: {},
      realmRoles: [],
      clientRoles: {},
      subGroups: []
    }]
  }
));

