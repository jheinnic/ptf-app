/* tslint:disable */
export class CreateCatInput {
    name?: string;
    age?: number;
}

export class Cat {
    id?: number;
    name?: string;
    age?: number;
}

export class DemoThang {
    hasNow: boolean;
    hadThen: boolean;
}

export abstract class IMutation {
    abstract createCat(createCatInput?: CreateCatInput): Cat | Promise<Cat>;
}

export abstract class IQuery {
    abstract getCats(): Cat[] | Promise<Cat[]>;

    abstract cat(id: string): Cat | Promise<Cat>;

    abstract countEm(): DemoThang[] | Promise<DemoThang[]>;

    abstract temp__(): boolean | Promise<boolean>;
}

export abstract class ISubscription {
    abstract catCreated(): Cat | Promise<Cat>;
}
