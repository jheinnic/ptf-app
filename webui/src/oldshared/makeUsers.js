const cc = require('chance').Chance();
const az = 'abcdefghijklmnopqrstuvwxyz';
const AZ = az.toUpperCase();
const digits = '0123456789';

const roles = ['Admin', 'Analyst', 'Contractor', 'Owner', 'Moderator', 'Editor', 'Contributor', 'Manager', 'Consumer', 'Follower', 'Subscriber', 'Captain', 'Vendor', 'Support'] 
const normal = {
  account: ["manage-account"]
};
const admin = {
  "realm-management": ["realm-admin"],
  account: ["manage-account"]
};

const users = [];
let number = 1000;

while( number > 0 ) {
   number = number - 1;
   let clientRoles = normal;
   let realmRoles = cc.pick(roles, (1 + cc.rpg('1d3')[0]));
   realmRoles.pop();
   // console.log(realmRoles);
   if ('Admin' in realmRoles) {
       clientRoles = admin;
       realmRoles = ['Admin'];
   } else if (! 'Support' in realmRoles) {
       realmRoles.push('User');
   }

   const name = cc.name();
   const names = name.split(' ');
   const password = cc.shuffle([cc.pick(az), cc.pick(digits), cc.pick(AZ), cc.string('1234567')].join('').split('')).join('');
   user = {
     username: cc.word("abcdefgh"),
     enabled: true,
     email: cc.email(),
     firstName: names[0],
     lastName: names[1],
     credentials: [
       { type: "password",
         value: password } ],
     realmRoles: realmRoles,
     clientRoles: clientRoles
   };
   users.push(user);
}

/*
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
*/

console.log(JSON.stringify({
  users: users
}));

/*
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
*/

