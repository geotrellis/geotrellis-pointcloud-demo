#
# S3 resources
#
data "template_file" "read_only_bucket_policy" {
  template = "${file("policies/s3-read-only-anonymous-user.json")}"

  vars {
    bucket = "${lower(var.environment)}-pgw-cm-site-${var.aws_region}"
  }
}

resource "aws_s3_bucket" "site" {
  bucket = "${lower(var.environment)}-pgw-cm-site-${var.aws_region}"
  policy = "${data.template_file.read_only_bucket_policy.rendered}"

  tags {
    Project     = "${var.project}"
    Environment = "${var.environment}"
  }
}

resource "aws_s3_bucket" "logs" {
  bucket = "${lower(var.environment)}-pgw-cm-logs-${var.aws_region}"
  acl    = "log-delivery-write"

  tags {
    Project     = "${var.project}"
    Environment = "${var.environment}"
  }
}
