# CMR Playback

Runs a script of invocations

## Usage

Run from the project root with this

```bash
clj -M:run data/script.edn -u https://cmr.sit.earthdata.nasa.gov -t $AUTH_TOKEN
```

```bash
clj -T:build uber
java -jar cmr-playback.jar script.edn -u <url> -t <auth token>
```

## Script

At present only EDN is supported.

```clojure
{:description "a description of the script"
 :steps [
    {:action :action-type
     :with {:key :val}}
 ]
}
```

## Available Script Commands

### CMR Steps

#### Ingest
| Action        | Options         | Description                          |
|---------------|-----------------|--------------------------------------|
| `:cmr/ingest` | `:concept-type` |                                      |
|               | `:file`         | File location relative to the script |
|               | `:provider`     | Provider ID                          |
|               | `:format`       | Concept format as keyword            |
|               | `:native-id`    | native-id of the concept             |

```clojure
{:action :cmr/ingest
 :with {:concept-type :collection
        :file "collection.echo10"
        :provider "CMR_ONLY"
        :format :echo10
        :native-id "my_collection"}}
```

#### Delete
| Action        | Options         | Description                          |
|---------------|-----------------|--------------------------------------|
| `:cmr/delete` | `:concept-type` |                                      |
|               | `:provider`     | Provider ID                          |
|               | `:native-id`    | native-id of the concept             |

```clojure
{:action :cmr/delete
 :with {:concept-type :collection
        :provider "CMR_ONLY"
        :native-id "my_collection"}}
```

### Elasticsearch

#### Delete By Query
| Action                | Options  | Description      |
|-----------------------|----------|------------------|
| `:es/delete-by-query` | `:host`  | ES Host and port |
|                       | `:index` | Index            |
|                       | `:query` | EDN es query     |

```clojure
{:action :es/delete-by-query
 :with {:host "http://localhost:9200"
        :index "my_index"
        :query {:query {:match {:native-id "my_collection"}}}}}
```

### General

#### Wait

| Action  | Options     | Description               |
|---------|-------------|---------------------------|
| `:wait` | `:duration` | Duration as a string "1s" |
|         | `:message`  | Optional message          |

```clojure
{:action :wait
 :with {:duration "3s"
        :message "Waiting for 3 seconds"}}
```

#### Say

| Action | Options    | Description |
|--------|------------|-------------|
| `:say` | `:message` | Message     |

```clojure
{:action :say
 :with {:message "your message"}}
```
