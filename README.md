# JavaPromise

## SYNOPSIS
    Promise promise = new Promise();
    Promise.Function funcOne = new Promise.Promise() {
        protected void impl(Object val) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("foo", String.valueOf(val));
            AsyncProccess.wget('http://hoge.com/huga', params, new Callback() {
                public void acceptCallback(Object hugaResult) {
                    getNext().call(hugaResult);
                }
            });
        }
    };

    Promise.Function funcTwo = new Promise.Promise() {
        protected void impl(Object val) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("hugaResult_is", String.valueOf(val));
            AsyncProccess.wget('http://hoge.com/moga', new Callback() {
                public void acceptCallback(Object mogaResult) {
                    getNext().call(mogaResult);
                }
            });
        }
    };

    promise.bind(funcOne, funcTwo).run();

## DESCRIPTION
JavaPromise enable to describe nested callback(async) proccess as pipe-line proccess.

This code partially derived from brook.promise in [brook.js](https://github.com/hirokidaichi/brook)
