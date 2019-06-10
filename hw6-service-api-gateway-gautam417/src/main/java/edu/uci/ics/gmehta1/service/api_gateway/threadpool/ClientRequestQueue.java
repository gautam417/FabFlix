package edu.uci.ics.gmehta1.service.api_gateway.threadpool;

public class ClientRequestQueue {
    private ListNode head;
    private ListNode tail;

    public ClientRequestQueue() {
        head=tail=null;
    }

    public synchronized void enqueue(ClientRequest clientRequest) {
        ListNode node = new ListNode (clientRequest, null);
        notify();
        if (isEmpty()){
            this.head = this.tail = node;
            return;
        }
        this.tail.setNext(node);
        this.tail=node;
    }

    public synchronized ClientRequest dequeue() throws InterruptedException {
        if (this.head == null)
        {
            try
            {
                wait();
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        ListNode node = this.head;
        this.head = this.head.getNext();
        return node.getClientRequest();
    }
    boolean isEmpty() {
        return head == null;
    }
    boolean isFull() {
        return false;
    }
}
