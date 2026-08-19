# istat-ndc-sample-service

Pilot service for the platform build-and-deploy chain. Deliberately trivial — one endpoint —
so that a failure during the pilot points at the pipeline rather than at application code.

```
GET /health  →  {"status":"UP","service":"sample-service","environment":"dev","imageTag":"<sha>"}
```

`imageTag` is echoed back by the running container, which is what makes a deploy verifiable:
the value returned by the live service must be the tag that was just released.

## Layout

```
src/…  Dockerfile                 rootless UBI runtime, works under the restricted-v2 SCC
.github/workflows/build.yaml      build → ghcr.io/ndc-dxc/istat-ndc-sample-service:<sha>
.github/workflows/release-prod.yaml  renders the production release artifact
deploy/                           its own deployment configuration
  Chart.yaml                        depends on the istat-ndc-service library chart
  values.yaml                       shared base
  values-{dev,test,prod}.yaml       per-environment deltas only
```

The environment is a file, not a branch: the same image travels from dev to test to prod, and
only the values file applied changes.

## Deploying

Deploys are performed by the delivery pipeline, which passes the image tag:

```sh
helm upgrade --install sample-service deploy/ \
  -n istat-ndc-dev -f deploy/values.yaml -f deploy/values-dev.yaml \
  --set image.tag=<sha> --atomic --wait
```

The build workflow can also ask the pipeline to do it automatically, by POSTing a signed
payload to the listener when `DEPLOY_WEBHOOK_URL` is configured for the repository.

## Promotion to production

We deploy to dev and test. Production runs on a **different cluster**, and promoting to it is
the ISTAT DevOps team's decision and responsibility — this repository holds no credentials for
it (`istat-ndc-cicd/docs/cross-cluster.md`).

What we hand them is a release artifact, not instructions:

```sh
gh workflow run release-prod.yaml -f revision=<sha validated in test> -f version=v1.2.3
```

It resolves the **digest** of that build, renders the manifests for both colours, and attaches
them to a GitHub release together with a promotion record. The digest matters because their
cluster may pull from a mirrored registry, where the same tag is a different lookup — the digest
is the artifact itself, so "what went to production is what test validated" becomes checkable
rather than assumed.

`deploy/values-prod.yaml` describes production but is applied by them, so it requires a review
from their team: see `.github/CODEOWNERS` and `istat-ndc-cicd/docs/repo-governance.md`.
