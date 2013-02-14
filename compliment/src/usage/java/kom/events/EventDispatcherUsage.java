/*
 * Copyright 2013 Sergey Yungman (aka komelgman)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kom.events;

import kom.util.callback.Callback;
import kom.util.callback.RunnableCallback;
import kom.util.callback.RunnableCallbackExecutor;

public class EventDispatcherUsage {

    EventDispatcher<UBaseEvent> dispatcher;

    public static void main(String[] args) {
        EventDispatcherUsage usage = new EventDispatcherUsage();
        usage.init();
        usage.example1();
        usage.example2();
        usage.example3();
    }

    private void example1() {
        System.out.println("Example 1: Handle event by type");
        dispatcher.addEventListener(U1Event.class, new Callback<U1Event>() {
            @Override
            public void handle(U1Event data) {
                System.out.println(data.toString());
            }
        });

        dispatcher.dispatchEvent(new U1Event("Hello world!!!"));
        dispatcher.dispatchEvent(new U2Event("This message not handled", 1));

        dispatcher.removeEventListeners();
    }

    private void example2() {
        System.out.println("Example 2: Handle event by super type");
        dispatcher.addEventListener(UBaseEvent.class, new Callback<UBaseEvent>() {
            @Override
            public void handle(UBaseEvent data) {
                System.out.println(data.toString());
            }
        });

        dispatcher.dispatchEvent(new U1Event("Hello world!!!"));
        dispatcher.dispatchEvent(new U2Event("Bye, bye", 1));

        dispatcher.removeEventListeners();
    }

    private void example3() {
        System.out.println("Example 3: Runnable Callback");
        dispatcher.addEventListener(UBaseEvent.class, new RunnableCallback<UBaseEvent>() {

            private UBaseEvent event;

            @Override
            public void handle(UBaseEvent data) {
                System.out.println("received data: " + data.toString());

                event = data;
            }

            @Override
            public void run() {
                if (event instanceof U1Event) {
                    System.out.println("Process U1Event");
                } else if (event instanceof U2Event) {
                    System.out.println("Process U2Event");
                }
            }
        });


        ((EventDispatcherImpl) dispatcher).setCallbackExecutor(new RunnableCallbackExecutor());

        dispatcher.dispatchEvent(new U1Event("Hello world!!!"));
        dispatcher.dispatchEvent(new U2Event("Bye, bye", 1));

        dispatcher.removeEventListeners();
    }


    private void init() {
        dispatcher = new EventDispatcherImpl<UBaseEvent>();
    }

    private class U1Event extends UBaseEvent {
        private final String text;

        public U1Event(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private class U2Event extends UBaseEvent {
        private final String text;
        private final int i;

        public U2Event(String text, int i) {
            this.text = text;
            this.i = i;
        }

        @Override
        public String toString() {
            return text + " -> id =" + i;
        }
    }


    private class UBaseEvent implements Event {

    }
}
