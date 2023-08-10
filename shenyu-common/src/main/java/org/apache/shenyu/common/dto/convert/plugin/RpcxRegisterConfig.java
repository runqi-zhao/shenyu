package org.apache.shenyu.common.dto.convert.plugin;

import java.io.Serializable;

public class RpcxRegisterConfig implements Serializable {

    private static final long serialVersionUID = 8265565809589255867L;

        private String register;

        private String threadpool;

        private Integer corethreads;

        private Integer threads;

        private Integer queues;

        /**
        * get threadpool.
        *
        * @return threadpool
        */
        public String getThreadpool() {
            return threadpool;
        }

        /**
        * set threadpool.
        *
        * @param threadpool threadpool
        */
        public void setThreadpool(final String threadpool) {
            this.threadpool = threadpool;
        }

        /**
        * get register.
        *
        * @return register
        */
        public String getRegister() {
            return register;
        }

        /**
        * set register.
        *
        * @param register register
        */
        public void setRegister(final String register) {
            this.register = register;
        }

        /**
        * get corethreads.
        *
        * @return corethreads
        */
        public Integer getCorethreads() {
            return corethreads;
        }

        /**
        * set corethreads.
        *
        * @param corethreads corethreads
        */
        public void setCorethreads(final Integer corethreads) {
            this.corethreads = corethreads;
        }

        /**
        * get threads.
        *
        * @return threads
        */
        public Integer getThreads() {
            return threads;
        }

        /**
        * set threads.
        *
        * @param threads threads
        */
        public void setThreads(final Integer threads) {
            this.threads = threads;
        }

        /**
        * get queues.
        *
        * @return queues
        */
        public Integer getQueues() {
            return queues;
        }

        /**
        * set queues.
        *
        * @param queues queues
        */
        public void setQueues(final Integer queues) {
            this.queues = queues;
        }
}
