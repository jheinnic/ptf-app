import { Module } from '@nestjs/common';
import { GraphQLModule } from '@nestjs/graphql';
import * as path from 'path';

@Module({
   imports: [
      GraphQLModule.forRoot({
         typePaths: ['./**/*.graphql'],
         debug: true,
         playground: true,
         installSubscriptionHandlers: true,
         definitions: {
            path: path.join(process.cwd(), 'packages', 'gql-api', 'src', 'graphql.schema.ts'),
            outputAs: 'class'
         }
      }),
   ],
})
export class ApplicationModule { }
