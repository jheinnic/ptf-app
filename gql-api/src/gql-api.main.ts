import 'reflect-metadata';
import {NestFactory} from '@nestjs/core';
import { ApplicationModule } from './application.module';
import { ValidationPipe } from '@nestjs/common';

async function bootstrap()
{
   console.log('Starting app context');
   const app = await NestFactory.create(ApplicationModule);
   console.log('Awaited application');
   app.useGlobalPipes(new ValidationPipe());
   await app.listen(3100);
   console.log('Listening...');
}

bootstrap()
   .then(() => {
      console.log('Bootstrap completed!');
   })
   .catch((err: any) => {
      console.error('Bootstrap failed!', err);
   });

console.log('Returned from async bootstrap.');
let counter = 0;
setInterval(() => {
   counter++;
   // console.log('Wooga wooga', ++counter);
}, 15000);

console.log('Returned from async keepalive.');

