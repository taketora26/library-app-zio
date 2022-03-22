# library-app-zio
This is ZIO with PlayFramework Sample code.

# References
- [Zio ZLayer with playframework](https://medium.com/@aadelegue/zio-zlayer-with-playframework-8e393574bea7)
- [play-zio](https://github.com/larousso/play-zio)
- [ZIOのZLayerについて](https://takatorix.hatenablog.com/entry/2020/12/23/230934)
- 
## Start the app

```
sbt 'run'
```

## Use the app 

- create 
```bash
curl -XPOST http://localhost:9000/mybooks -H 'Content-Type:application/json' -d '{"name":"mybook"}'
```

- getById
```bash
curl -XGET http://localhost:9000/mybooks/fc788402-299f-4e4b-87f4-b5b6d1e7d6e6 -H 'Content-Type:application/json'
```

- list
```bash
curl http://localhost:9000/mybooks
```

- update
```bash
curl -XPUT http://localhost:9000/mybooks/fc788402-299f-4e4b-87f4-b5b6d1e7d6e6 -H 'Content-Type:application/json' -d '{"id":"fc788402-299f-4e4b-87f4-b5b6d1e7d6e6","name":"my zio book"}'
```