export class Node<T> {
  value: T;
  next: Node<T> | null = null;

  constructor(value: T) {
    this.value = value;
  }
}

export class LinkedList<T> {
  head: Node<T> | null = null;
  private length = 0;

  append(value: T): void {
    const newNode = new Node(value);
    if (!this.head) {
      this.head = newNode;
    } else {
      let current = this.head;
      while (current.next) {
        current = current.next;
      }
      current.next = newNode;
    }
    this.length++;
  }

  prepend(value: T): void {
    const newNode = new Node(value);
    newNode.next = this.head;
    this.head = newNode;
    this.length++;
  }

  remove(predicate: (value: T) => boolean): T | null {
    if (!this.head) return null;

    if (predicate(this.head.value)) {
      const removed = this.head.value;
      this.head = this.head.next;
      this.length--;
      return removed;
    }

    let current = this.head;
    while (current.next && !predicate(current.next.value)) {
      current = current.next;
    }

    if (current.next) {
      const removed = current.next.value;
      current.next = current.next.next;
      this.length--;
      return removed;
    }

    return null;
  }

  toArray(): T[] {
    const result: T[] = [];
    let current = this.head;
    while (current) {
      result.push(current.value);
      current = current.next;
    }
    return result;
  }

  static fromArray<T>(items: T[]): LinkedList<T> {
    const list = new LinkedList<T>();
    for (const item of items) {
      list.append(item);
    }
    return list;
  }

  size(): number {
    return this.length;
  }
}

export class Stack<T> {
  private items: T[] = [];

  push(element: T): void {
    this.items.push(element);
  }

  pop(): T | undefined {
    return this.items.pop();
  }

  peek(): T | undefined {
    return this.items[this.items.length - 1];
  }

  isEmpty(): boolean {
    return this.items.length === 0;
  }

  size(): number {
    return this.items.length;
  }

  toArray(): T[] {
    return [...this.items];
  }

  static fromArray<T>(items: T[]): Stack<T> {
    const stack = new Stack<T>();
    for (const item of items) {
      stack.push(item);
    }
    return stack;
  }
}

export class Queue<T> {
  private items: T[] = [];

  enqueue(element: T): void {
    this.items.push(element);
  }

  dequeue(): T | undefined {
    return this.items.shift();
  }

  peek(): T | undefined {
    return this.items[0];
  }

  isEmpty(): boolean {
    return this.items.length === 0;
  }

  size(): number {
    return this.items.length;
  }

  toArray(): T[] {
    return [...this.items];
  }

  static fromArray<T>(items: T[]): Queue<T> {
    const queue = new Queue<T>();
    for (const item of items) {
      queue.enqueue(item);
    }
    return queue;
  }
}
